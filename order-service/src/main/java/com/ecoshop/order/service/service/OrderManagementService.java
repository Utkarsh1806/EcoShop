package com.ecoshop.order.service.service;

import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.order.service.domain.*;
import com.ecoshop.order.service.dto.OrderDtos.*;
import com.ecoshop.order.service.event.OrderEvents.*;
import static com.ecoshop.order.service.event.OrderEvents.TOPIC_ORDER_CREATED;
import static com.ecoshop.order.service.event.OrderEvents.TOPIC_ORDER_STATUS_CHANGED;
import com.ecoshop.order.service.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderManagementService {

    private static final Logger log = LoggerFactory.getLogger(OrderManagementService.class);
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = buildTransitions();

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public OrderResponse placeOrder(UUID userId, PlaceOrderRequest req) {
        BigDecimal subtotal = req.items().stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = req.discount() != null ? req.discount() : BigDecimal.ZERO;
        BigDecimal tax = req.tax() != null ? req.tax() : BigDecimal.ZERO;
        BigDecimal shipping = req.shippingCost() != null ? req.shippingCost() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount).add(tax).add(shipping);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("INVALID_TOTAL", "Order total cannot be negative");
        }

        ShippingAddressDto addr = req.shippingAddress();
        ShippingAddress shipAddr = ShippingAddress.builder()
                .recipientName(addr.recipientName())
                .phone(addr.phone())
                .line1(addr.line1())
                .line2(addr.line2())
                .city(addr.city())
                .state(addr.state())
                .postalCode(addr.postalCode())
                .country(addr.country() != null ? addr.country() : "IN")
                .build();

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .status(OrderStatus.PENDING_PAYMENT)
                .subtotal(subtotal)
                .discount(discount)
                .tax(tax)
                .shippingCost(shipping)
                .totalAmount(total)
                .currency(req.currency() != null ? req.currency() : "INR")
                .couponCode(req.couponCode())
                .shippingAddress(shipAddr)
                .build();

        for (PlaceOrderItemDto i : req.items()) {
            BigDecimal lineTotal = i.unitPrice().multiply(BigDecimal.valueOf(i.quantity()));
            order.addItem(OrderItem.builder()
                    .productId(i.productId())
                    .variantId(i.variantId())
                    .sku(i.sku())
                    .productName(i.productName())
                    .thumbnailUrl(i.thumbnailUrl())
                    .unitPrice(i.unitPrice())
                    .quantity(i.quantity())
                    .lineTotal(lineTotal)
                    .build());
        }
        order.addStatusHistory(OrderStatusHistory.builder()
                .toStatus(OrderStatus.PENDING_PAYMENT)
                .note("Order placed")
                .changedBy("system")
                .build());

        order = orderRepository.save(order);
        log.info("Order placed: {} ({}) total={}", order.getOrderNumber(), order.getId(), total);

        // Publish event
        OrderCreatedEvent evt = new OrderCreatedEvent(
                UUID.randomUUID(), Instant.now(), order.getId(), order.getOrderNumber(),
                userId, total, order.getCurrency()
        );
        kafkaTemplate.send(TOPIC_ORDER_CREATED, order.getId().toString(), evt);

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND",
                        "Order " + orderId + " not found"));
        if (!order.getUserId().equals(userId)) {
            throw BusinessException.badRequest("FORBIDDEN", "Not your order");
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getByNumber(UUID userId, String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND",
                        "No order with number " + orderNumber));
        if (!order.getUserId().equals(userId)) {
            throw BusinessException.badRequest("FORBIDDEN", "Not your order");
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listForUser(UUID userId, int page, int size) {
        Page<Order> result = orderRepository.findByUserId(
                userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<OrderResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    /** Internal — called by payment-service via Kafka or webhook */
    @Transactional
    public OrderResponse markPaid(UUID orderId, String paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND",
                        "Order " + orderId + " not found"));
        order.setPaymentId(paymentId);
        order.setPaymentStatus("SUCCEEDED");
        return transitionTo(order, OrderStatus.PAID, "Payment captured: " + paymentId, "payment-service");
    }

    @Transactional
    public OrderResponse markFailed(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND",
                        "Order " + orderId + " not found"));
        order.setPaymentStatus("FAILED");
        return transitionTo(order, OrderStatus.FAILED, reason, "payment-service");
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, UpdateStatusRequest req, String actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND",
                        "Order " + orderId + " not found"));
        return transitionTo(order, req.status(), req.note(), actor);
    }

    @Transactional
    public OrderResponse cancel(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND",
                        "Order " + orderId + " not found"));
        if (!order.getUserId().equals(userId)) {
            throw BusinessException.badRequest("FORBIDDEN", "Not your order");
        }
        return transitionTo(order, OrderStatus.CANCELLED, "Cancelled by user", userId.toString());
    }

    private OrderResponse transitionTo(Order order, OrderStatus next, String note, String actor) {
        OrderStatus current = order.getStatus();
        if (current == next) return toResponse(order);
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw BusinessException.badRequest("INVALID_TRANSITION",
                    "Cannot transition from " + current + " to " + next);
        }
        order.setStatus(next);
        order.addStatusHistory(OrderStatusHistory.builder()
                .fromStatus(current).toStatus(next)
                .note(note).changedBy(actor)
                .build());
        log.info("Order {} status: {} → {}", order.getOrderNumber(), current, next);

        OrderStatusChangedEvent evt = new OrderStatusChangedEvent(
                UUID.randomUUID(), Instant.now(), order.getId(), order.getOrderNumber(),
                current.name(), next.name()
        );
        kafkaTemplate.send(TOPIC_ORDER_STATUS_CHANGED, order.getId().toString(), evt);

        return toResponse(order);
    }

    private String generateOrderNumber() {
        // Real systems use a dedicated number generator service / DB sequence.
        // Acceptable for an MVP; collisions are guarded by the unique constraint.
        return "ORD-" + Year.now().getValue() + "-" +
                String.format("%010d", ThreadLocalRandom.current().nextLong(9_999_999_999L));
    }

    private OrderResponse toResponse(Order o) {
        ShippingAddress sa = o.getShippingAddress();
        ShippingAddressDto saDto = sa == null ? null : new ShippingAddressDto(
                sa.getRecipientName(), sa.getPhone(), sa.getLine1(), sa.getLine2(),
                sa.getCity(), sa.getState(), sa.getPostalCode(), sa.getCountry()
        );
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(i.getId(), i.getProductId(), i.getVariantId(),
                        i.getSku(), i.getProductName(), i.getThumbnailUrl(),
                        i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
                .toList();
        return new OrderResponse(
                o.getId(), o.getOrderNumber(), o.getUserId(), o.getStatus(),
                o.getSubtotal(), o.getDiscount(), o.getTax(), o.getShippingCost(),
                o.getTotalAmount(), o.getCurrency(), o.getCouponCode(),
                o.getPaymentId(), o.getPaymentStatus(), saDto, items,
                o.getCreatedAt(), o.getUpdatedAt()
        );
    }

    private static Map<OrderStatus, Set<OrderStatus>> buildTransitions() {
        Map<OrderStatus, Set<OrderStatus>> m = new EnumMap<>(OrderStatus.class);
        m.put(OrderStatus.PENDING_PAYMENT, Set.of(OrderStatus.PAID, OrderStatus.FAILED, OrderStatus.CANCELLED));
        m.put(OrderStatus.PAID, Set.of(OrderStatus.PACKED, OrderStatus.CANCELLED, OrderStatus.REFUNDED));
        m.put(OrderStatus.PACKED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        m.put(OrderStatus.SHIPPED, Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.RETURNED));
        m.put(OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED, OrderStatus.RETURNED));
        m.put(OrderStatus.DELIVERED, Set.of(OrderStatus.RETURNED));
        m.put(OrderStatus.RETURNED, Set.of(OrderStatus.REFUNDED));
        m.put(OrderStatus.FAILED, Set.of());
        m.put(OrderStatus.CANCELLED, Set.of(OrderStatus.REFUNDED));
        m.put(OrderStatus.REFUNDED, Set.of());
        return m;
    }
}
