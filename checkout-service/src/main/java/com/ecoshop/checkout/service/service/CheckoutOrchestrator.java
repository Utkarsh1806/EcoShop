package com.ecoshop.checkout.service.service;

import com.ecoshop.checkout.service.client.*;
import com.ecoshop.checkout.service.client.CartClient.CartView;
import com.ecoshop.checkout.service.client.CartClient.CartItemView;
import com.ecoshop.checkout.service.client.InventoryClient.AvailabilityCheckRequest;
import com.ecoshop.checkout.service.client.InventoryClient.AvailabilityCheckResponse;
import com.ecoshop.checkout.service.client.InventoryClient.AvailabilityResult;
import com.ecoshop.checkout.service.client.InventoryClient.ReserveItem;
import com.ecoshop.checkout.service.client.InventoryClient.ReserveRequest;
import com.ecoshop.checkout.service.client.InventoryClient.ReservationResponse;
import com.ecoshop.checkout.service.client.PricingClient.QuoteRequest;
import com.ecoshop.checkout.service.client.PricingClient.QuoteResponse;
import com.ecoshop.checkout.service.client.PricingClient.RedeemRequest;
import com.ecoshop.checkout.service.client.ShippingClient.RateQuoteRequest;
import com.ecoshop.checkout.service.client.ShippingClient.RateQuoteResponse;
import com.ecoshop.checkout.service.client.OrderClient.OrderItemDto;
import com.ecoshop.checkout.service.client.OrderClient.PlaceOrderRequest;
import com.ecoshop.checkout.service.client.PaymentClient.CreatePaymentRequest;
import com.ecoshop.checkout.service.client.PaymentClient.PaymentResponse;
import com.ecoshop.checkout.service.domain.CheckoutSession;
import com.ecoshop.checkout.service.domain.CheckoutStatus;
import com.ecoshop.checkout.service.dto.CheckoutDtos.*;
import com.ecoshop.checkout.service.repo.CheckoutSessionRepository;
import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Saga orchestrator. Steps:
 *
 *   1. Load cart from cart-service
 *   2. Check availability via inventory-service
 *   3. Reserve stock (TTL 15 min)
 *   4. If coupon present, quote discount via pricing-promotion-service
 *   5. Get shipping rate via shipping-service
 *   6. Compute tax (simple flat % for now)
 *   7. Place order via order-service
 *   8. Redeem coupon via pricing-promotion-service (idempotent on orderId)
 *   9. Create payment intent via payment-service
 *  10. Return checkout response with client_secret / checkout_url
 *
 * Compensation on failure: release stock reservation, mark session FAILED.
 *
 * Idempotency: client supplies idempotencyKey. Re-running with same key returns existing session.
 */
@Service
@RequiredArgsConstructor
public class CheckoutOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(CheckoutOrchestrator.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.18"); // 18% GST placeholder
    private static final long RESERVATION_TTL_SECONDS = 15 * 60; // 15 min hold

    private final CheckoutSessionRepository sessionRepo;
    private final CartClient cartClient;
    private final InventoryClient inventoryClient;
    private final PricingClient pricingClient;
    private final ShippingClient shippingClient;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;

    @Transactional
    public CheckoutResponse start(UUID userId, String authHeader, StartCheckoutRequest req) {
        // Idempotency
        var existing = sessionRepo.findByIdempotencyKey(req.idempotencyKey());
        if (existing.isPresent()) {
            log.info("Idempotent replay for key {}: returning existing checkout {}",
                    req.idempotencyKey(), existing.get().getId());
            return toResponse(existing.get());
        }

        // 0. Persist starting session
        AddressDto addr = req.shippingAddress();
        CheckoutSession session = CheckoutSession.builder()
                .userId(userId)
                .cartKey(req.cartKey())
                .idempotencyKey(req.idempotencyKey())
                .status(CheckoutStatus.STARTED)
                .couponCode(req.couponCode())
                .currency("INR")
                .shipRecipientName(addr.recipientName())
                .shipPhone(addr.phone())
                .shipLine1(addr.line1())
                .shipLine2(addr.line2())
                .shipCity(addr.city())
                .shipState(addr.state())
                .shipPostalCode(addr.postalCode())
                .shipCountry(addr.country() != null ? addr.country() : "IN")
                .build();
        session = sessionRepo.save(session);

        try {
            return runSaga(session, req, authHeader);
        } catch (BusinessException be) {
            session.setStatus(CheckoutStatus.FAILED);
            session.setFailureReason(be.getMessage());
            sessionRepo.save(session);
            compensate(session);
            throw be;
        } catch (Exception e) {
            log.error("Checkout {} failed unexpectedly", session.getId(), e);
            session.setStatus(CheckoutStatus.FAILED);
            session.setFailureReason("Unexpected error: " + e.getMessage());
            sessionRepo.save(session);
            compensate(session);
            throw BusinessException.badRequest("CHECKOUT_FAILED",
                    "Checkout failed: " + e.getMessage());
        }
    }

    private CheckoutResponse runSaga(CheckoutSession session, StartCheckoutRequest req, String authHeader) {
        // 1. Load cart
        CartView cart = unwrap(cartClient.getCart(req.cartKey()), "cart-service");
        if (cart.items() == null || cart.items().isEmpty()) {
            throw BusinessException.badRequest("EMPTY_CART", "Cart is empty");
        }
        log.info("[checkout {}] cart loaded: {} items, subtotal {}",
                session.getId(), cart.items().size(), cart.subtotal());

        // 2. Availability check (informational; reserve does the real check atomically)
        List<ReserveItem> items = cart.items().stream()
                .filter(i -> i.sku() != null)
                .map(i -> new ReserveItem(i.sku(), i.quantity()))
                .toList();
        if (items.isEmpty()) {
            throw BusinessException.badRequest("NO_SKUS",
                    "Cart items don't have SKUs — variants required for stock tracking");
        }
        AvailabilityCheckResponse availability = unwrap(
                inventoryClient.checkAvailability(new AvailabilityCheckRequest(items)),
                "inventory-service");
        if (!availability.allAvailable()) {
            String missing = availability.details().stream()
                    .filter(r -> !r.sufficient())
                    .map(AvailabilityResult::sku).toList().toString();
            throw BusinessException.conflict("OUT_OF_STOCK",
                    "Insufficient stock for SKUs: " + missing);
        }

        // 3. Reserve stock (use checkout id as ref so we can release on failure)
        ReservationResponse reservation = unwrap(inventoryClient.reserve(new ReserveRequest(
                "checkout:" + session.getId(),
                "CHECKOUT",
                items,
                RESERVATION_TTL_SECONDS
        )), "inventory-service");
        session.setReservationId(reservation.id());
        session.setStatus(CheckoutStatus.STOCK_RESERVED);
        sessionRepo.save(session);
        log.info("[checkout {}] reserved stock, reservation {}", session.getId(), reservation.id());

        // 4. Apply coupon (optional)
        BigDecimal subtotal = cart.subtotal();
        BigDecimal discount = BigDecimal.ZERO;
        if (req.couponCode() != null && !req.couponCode().isBlank()) {
            QuoteResponse quote = unwrap(pricingClient.quote(new QuoteRequest(
                    req.couponCode(), session.getUserId(), subtotal
            )), "pricing-promotion-service");
            if (quote.valid()) {
                discount = quote.discountAmount();
                log.info("[checkout {}] coupon {} valid, discount {}",
                        session.getId(), req.couponCode(), discount);
            } else {
                log.info("[checkout {}] coupon {} not applicable: {}",
                        session.getId(), req.couponCode(), quote.message());
            }
        }

        // 5. Shipping rate
        RateQuoteResponse rate = unwrap(shippingClient.rate(new RateQuoteRequest(
                "560001",
                req.shippingAddress().postalCode(),
                Math.max(500, items.stream().mapToInt(ReserveItem::quantity).sum() * 500) // rough weight
        )), "shipping-service");
        BigDecimal shippingCost = rate.cost();

        // 6. Tax
        BigDecimal tax = subtotal.subtract(discount).max(BigDecimal.ZERO)
                .multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.subtract(discount).add(tax).add(shippingCost)
                .setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);

        session.setSubtotal(subtotal);
        session.setDiscount(discount);
        session.setTax(tax);
        session.setShippingCost(shippingCost);
        session.setTotalAmount(total);
        session.setStatus(CheckoutStatus.PRICED);
        sessionRepo.save(session);
        log.info("[checkout {}] priced: subtotal={} discount={} tax={} shipping={} total={}",
                session.getId(), subtotal, discount, tax, shippingCost, total);

        // 7. Place order
        AddressDto a = req.shippingAddress();
        var orderAddr = new OrderClient.AddressDto(
                a.recipientName(), a.phone(), a.line1(), a.line2(),
                a.city(), a.state(), a.postalCode(), a.country() != null ? a.country() : "IN"
        );
        List<OrderItemDto> orderItems = cart.items().stream()
                .map(i -> new OrderItemDto(
                        i.productId(), i.variantId(), i.sku(), i.name(),
                        i.thumbnailUrl(), i.unitPrice(), i.quantity()))
                .toList();

        var orderResp = unwrap(orderClient.placeOrder(authHeader, new PlaceOrderRequest(
                orderItems, orderAddr, req.couponCode(),
                discount, tax, shippingCost, "INR"
        )), "order-service");
        session.setOrderId(orderResp.id());
        session.setOrderNumber(orderResp.orderNumber());
        session.setStatus(CheckoutStatus.ORDER_PLACED);
        sessionRepo.save(session);
        log.info("[checkout {}] order placed: {} ({})",
                session.getId(), orderResp.orderNumber(), orderResp.id());

        // 8. Redeem coupon (atomic now that we have an orderId)
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            try {
                pricingClient.redeem(new RedeemRequest(
                        req.couponCode(), session.getUserId(), orderResp.id(), subtotal));
                log.info("[checkout {}] coupon redeemed", session.getId());
            } catch (Exception e) {
                // Non-fatal: order is placed, just log. In production: emit event for reconciliation.
                log.warn("[checkout {}] coupon redeem failed (continuing): {}",
                        session.getId(), e.getMessage());
            }
        }

        // 9. Create payment intent
        PaymentResponse payment = unwrap(paymentClient.createPayment(authHeader,
                new CreatePaymentRequest(
                        orderResp.id(), total, "INR",
                        req.paymentMethod(),
                        "checkout:" + session.getId() // payment idempotency key
                )), "payment-service");
        session.setPaymentId(payment.id());
        session.setPaymentGatewayRef(payment.gatewayRef());
        session.setClientSecret(payment.clientSecret());
        session.setCheckoutUrl(payment.checkoutUrl());
        session.setStatus(CheckoutStatus.PAYMENT_PENDING);
        sessionRepo.save(session);

        log.info("[checkout {}] payment created: gatewayRef={}",
                session.getId(), payment.gatewayRef());

        // Note: cart is intentionally NOT cleared here. Frontend clears it after successful payment confirmation.

        return toResponse(session);
    }

    private void compensate(CheckoutSession session) {
        if (session.getReservationId() == null) return;
        try {
            inventoryClient.release("checkout:" + session.getId());
            log.info("[checkout {}] compensated: released reservation {}",
                    session.getId(), session.getReservationId());
        } catch (Exception e) {
            log.error("[checkout {}] compensation failed for reservation {}: {}",
                    session.getId(), session.getReservationId(), e.getMessage());
            // Reservation has TTL — will auto-expire even if release fails
        }
    }

    @Transactional(readOnly = true)
    public CheckoutResponse get(UUID id, UUID userId) {
        CheckoutSession s = sessionRepo.findById(id)
                .orElseThrow(() -> BusinessException.notFound("CHECKOUT_NOT_FOUND",
                        "Checkout " + id + " not found"));
        if (!s.getUserId().equals(userId)) {
            throw BusinessException.badRequest("FORBIDDEN", "Not your checkout");
        }
        return toResponse(s);
    }

    private <T> T unwrap(ApiResponse<T> resp, String service) {
        if (resp == null) {
            throw BusinessException.badRequest("DOWNSTREAM_NULL", service + " returned null");
        }
        if (!resp.success() || resp.data() == null) {
            throw BusinessException.badRequest("DOWNSTREAM_ERROR",
                    service + ": " + (resp.error() != null ? resp.error() : "unknown error"));
        }
        return resp.data();
    }

    public CheckoutResponse toResponse(CheckoutSession s) {
        return new CheckoutResponse(
                s.getId(), s.getStatus(),
                s.getSubtotal(), s.getDiscount(), s.getTax(), s.getShippingCost(),
                s.getTotalAmount(), s.getCurrency(),
                s.getOrderId(), s.getOrderNumber(),
                s.getPaymentId(), s.getPaymentGatewayRef(),
                s.getClientSecret(), s.getCheckoutUrl(),
                s.getFailureReason()
        );
    }
}
