package com.ecoshop.returns.service.service;

import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.returns.service.client.OrderClient;
import com.ecoshop.returns.service.client.OrderClient.OrderItemView;
import com.ecoshop.returns.service.client.OrderClient.OrderView;
import com.ecoshop.returns.service.client.PaymentClient;
import com.ecoshop.returns.service.client.PaymentClient.RefundRequest;
import com.ecoshop.returns.service.client.PaymentClient.RefundResponse;
import com.ecoshop.returns.service.domain.*;
import com.ecoshop.returns.service.dto.ReturnsDtos.*;
import com.ecoshop.returns.service.repo.ReturnRequestRepository;
import com.ecoshop.common.dto.ApiResponse;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ReturnsService {

    private static final Logger log = LoggerFactory.getLogger(ReturnsService.class);
    public static final String TOPIC_RETURN_CREATED = "return.created";
    public static final String TOPIC_RETURN_STATUS_CHANGED = "return.status.changed";
    public static final String TOPIC_RETURN_REFUNDED = "return.refunded";

    private static final Map<ReturnStatus, Set<ReturnStatus>> ALLOWED = transitions();

    private final ReturnRequestRepository repository;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ReturnResponse createReturn(UUID userId, String authHeader, CreateReturnRequest req) {
        // Fetch order to validate ownership and snapshot item details
        ApiResponse<OrderView> orderResp = orderClient.getById(authHeader, req.orderId());
        if (orderResp == null || !orderResp.success() || orderResp.data() == null) {
            throw BusinessException.notFound("ORDER_NOT_FOUND",
                    "Order " + req.orderId() + " not found or not accessible");
        }
        OrderView order = orderResp.data();
        if (!order.userId().equals(userId)) {
            throw BusinessException.badRequest("FORBIDDEN", "Not your order");
        }
        if (!"DELIVERED".equals(order.status())) {
            throw BusinessException.badRequest("INVALID_ORDER_STATE",
                    "Returns are only allowed for DELIVERED orders (current: " + order.status() + ")");
        }

        Map<UUID, OrderItemView> orderItemsById = new HashMap<>();
        for (OrderItemView i : order.items()) orderItemsById.put(i.id(), i);

        ReturnRequest returnRequest = ReturnRequest.builder()
                .rmaNumber(generateRmaNumber())
                .orderId(req.orderId())
                .userId(userId)
                .reason(req.reason())
                .reasonDetails(req.reasonDetails())
                .status(ReturnStatus.REQUESTED)
                .build();

        BigDecimal refundTotal = BigDecimal.ZERO;
        for (ReturnItemRequest it : req.items()) {
            OrderItemView orderItem = orderItemsById.get(it.orderItemId());
            if (orderItem == null) {
                throw BusinessException.badRequest("ITEM_NOT_IN_ORDER",
                        "Order item " + it.orderItemId() + " not in order " + req.orderId());
            }
            if (it.quantity() > orderItem.quantity()) {
                throw BusinessException.badRequest("QUANTITY_EXCEEDS_ORDER",
                        "Cannot return " + it.quantity() + " of item " + it.orderItemId() +
                        " (only " + orderItem.quantity() + " purchased)");
            }
            BigDecimal lineRefund = orderItem.unitPrice().multiply(BigDecimal.valueOf(it.quantity()));
            refundTotal = refundTotal.add(lineRefund);

            returnRequest.addItem(ReturnItem.builder()
                    .orderItemId(orderItem.id())
                    .productId(orderItem.productId())
                    .sku(orderItem.sku())
                    .productName(orderItem.productName())
                    .quantity(it.quantity())
                    .unitPrice(orderItem.unitPrice())
                    .lineRefundAmount(lineRefund)
                    .build());
        }
        returnRequest.setRefundAmount(refundTotal);
        returnRequest.addStatusHistory(ReturnStatusHistory.builder()
                .toStatus(ReturnStatus.REQUESTED)
                .note("Customer raised return request")
                .changedBy(userId.toString())
                .build());

        returnRequest = repository.save(returnRequest);
        log.info("Return {} created for order {} userId={} refundEstimate={}",
                returnRequest.getRmaNumber(), req.orderId(), userId, refundTotal);

        publishCreated(returnRequest);
        return toResponse(returnRequest);
    }

    @Transactional
    public ReturnResponse approve(UUID returnId, ApproveRequest req, String actor) {
        ReturnRequest r = findById(returnId);
        if (req.pickupScheduledAt() != null) r.setPickupScheduledAt(req.pickupScheduledAt());
        // In production: call shipping-service to schedule reverse logistics here
        r.setPickupTrackingNumber("RP-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        return transition(r, ReturnStatus.APPROVED, "Approved by " + actor, actor);
    }

    @Transactional
    public ReturnResponse reject(UUID returnId, RejectRequest req, String actor) {
        ReturnRequest r = findById(returnId);
        r.setRejectionReason(req.rejectionReason());
        return transition(r, ReturnStatus.REJECTED, req.rejectionReason(), actor);
    }

    @Transactional
    public ReturnResponse markPickupScheduled(UUID returnId, String actor) {
        return transition(findById(returnId), ReturnStatus.PICKUP_SCHEDULED, "Pickup scheduled", actor);
    }

    @Transactional
    public ReturnResponse markPickedUp(UUID returnId, String actor) {
        return transition(findById(returnId), ReturnStatus.PICKED_UP, "Item picked up", actor);
    }

    @Transactional
    public ReturnResponse recordQc(UUID returnId, QcResultRequest req, String actor) {
        ReturnRequest r = findById(returnId);
        if (r.getStatus() != ReturnStatus.PICKED_UP && r.getStatus() != ReturnStatus.QC_PENDING) {
            throw BusinessException.badRequest("INVALID_STATE",
                    "QC can only happen after pickup (current: " + r.getStatus() + ")");
        }
        r.setQcNote(req.note());
        ReturnStatus next = req.passed() ? ReturnStatus.QC_PASSED : ReturnStatus.QC_FAILED;
        return transition(r, next, req.note(), actor);
    }

    /**
     * Initiate refund via payment-service. Idempotent: subsequent calls return existing refund.
     * Transitions REFUND_INITIATED → REFUNDED on success, or back to QC_PASSED on failure.
     */
    @Transactional
    public ReturnResponse initiateRefund(UUID returnId, String actor, String authHeader) {
        ReturnRequest r = findById(returnId);
        if (r.getStatus() != ReturnStatus.QC_PASSED) {
            throw BusinessException.badRequest("INVALID_STATE",
                    "Refund can only be initiated after QC_PASSED");
        }
        // Move to REFUND_INITIATED first as a marker (in case the call below fails mid-flight)
        transition(r, ReturnStatus.REFUND_INITIATED, "Refund initiated", actor);

        // Need the paymentId for the order. Fetch from order-service.
        ApiResponse<OrderView> orderResp = orderClient.getById(authHeader, r.getOrderId());
        if (orderResp == null || !orderResp.success() || orderResp.data() == null) {
            throw BusinessException.badRequest("ORDER_FETCH_FAILED",
                    "Could not fetch order for refund");
        }
        String paymentIdStr = orderResp.data().paymentId();
        if (paymentIdStr == null || paymentIdStr.isBlank()) {
            throw BusinessException.badRequest("NO_PAYMENT_ON_ORDER",
                    "Order has no associated payment to refund");
        }
        UUID paymentId = UUID.fromString(paymentIdStr);

        try {
            ApiResponse<RefundResponse> refundResp = paymentClient.refund(
                    paymentId, new RefundRequest(r.getRefundAmount(), "Return RMA " + r.getRmaNumber()));
            if (refundResp == null || !refundResp.success() || refundResp.data() == null) {
                throw BusinessException.badRequest("REFUND_FAILED",
                        "Payment-service returned failure: " +
                                (refundResp != null ? refundResp.error() : "null"));
            }
            r.setRefundPaymentId(paymentId);
            transition(r, ReturnStatus.REFUNDED, "Refund processed: " + refundResp.data().status(), actor);
            publishRefunded(r);
            return toResponse(r);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Refund failed for return {}", returnId, e);
            throw BusinessException.badRequest("REFUND_FAILED", e.getMessage());
        }
    }

    @Transactional
    public ReturnResponse close(UUID returnId, String actor) {
        ReturnRequest r = findById(returnId);
        return transition(r, ReturnStatus.CLOSED, "Closed by " + actor, actor);
    }

    @Transactional(readOnly = true)
    public ReturnResponse get(UUID userId, UUID returnId) {
        ReturnRequest r = findById(returnId);
        if (!r.getUserId().equals(userId)) {
            throw BusinessException.badRequest("FORBIDDEN", "Not your return");
        }
        return toResponse(r);
    }

    @Transactional(readOnly = true)
    public ReturnResponse getByRma(String rmaNumber) {
        ReturnRequest r = repository.findByRmaNumber(rmaNumber)
                .orElseThrow(() -> BusinessException.notFound("RETURN_NOT_FOUND",
                        "RMA " + rmaNumber + " not found"));
        return toResponse(r);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReturnResponse> listMine(UUID userId, int page, int size) {
        Page<ReturnRequest> result = repository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size));
        List<ReturnResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    private ReturnRequest findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("RETURN_NOT_FOUND",
                        "Return " + id + " not found"));
    }

    private ReturnResponse transition(ReturnRequest r, ReturnStatus next, String note, String actor) {
        ReturnStatus current = r.getStatus();
        if (current == next) return toResponse(r);
        Set<ReturnStatus> allowed = ALLOWED.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw BusinessException.badRequest("INVALID_TRANSITION",
                    "Cannot transition from " + current + " to " + next);
        }
        r.setStatus(next);
        r.addStatusHistory(ReturnStatusHistory.builder()
                .fromStatus(current).toStatus(next).note(note).changedBy(actor).build());
        log.info("Return {} status: {} -> {}", r.getRmaNumber(), current, next);
        publishStatusChanged(r, current);
        return toResponse(r);
    }

    private String generateRmaNumber() {
        return "RMA-" + Year.now().getValue() + "-" +
                String.format("%010d", ThreadLocalRandom.current().nextLong(9_999_999_999L));
    }

    private void publishCreated(ReturnRequest r) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventId", UUID.randomUUID().toString());
        evt.put("occurredAt", Instant.now().toString());
        evt.put("returnId", r.getId().toString());
        evt.put("rmaNumber", r.getRmaNumber());
        evt.put("orderId", r.getOrderId().toString());
        evt.put("userId", r.getUserId().toString());
        evt.put("refundEstimate", r.getRefundAmount() != null ? r.getRefundAmount().toPlainString() : null);
        kafkaTemplate.send(TOPIC_RETURN_CREATED, r.getOrderId().toString(), evt);
    }

    private void publishStatusChanged(ReturnRequest r, ReturnStatus from) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventId", UUID.randomUUID().toString());
        evt.put("returnId", r.getId().toString());
        evt.put("rmaNumber", r.getRmaNumber());
        evt.put("orderId", r.getOrderId().toString());
        evt.put("fromStatus", from.name());
        evt.put("toStatus", r.getStatus().name());
        kafkaTemplate.send(TOPIC_RETURN_STATUS_CHANGED, r.getOrderId().toString(), evt);
    }

    private void publishRefunded(ReturnRequest r) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventId", UUID.randomUUID().toString());
        evt.put("returnId", r.getId().toString());
        evt.put("orderId", r.getOrderId().toString());
        evt.put("amount", r.getRefundAmount() != null ? r.getRefundAmount().toPlainString() : null);
        evt.put("paymentId", r.getRefundPaymentId() != null ? r.getRefundPaymentId().toString() : null);
        kafkaTemplate.send(TOPIC_RETURN_REFUNDED, r.getOrderId().toString(), evt);
    }

    public ReturnResponse toResponse(ReturnRequest r) {
        List<ReturnItemResponse> items = r.getItems().stream()
                .map(i -> new ReturnItemResponse(i.getId(), i.getOrderItemId(), i.getProductId(),
                        i.getSku(), i.getProductName(), i.getQuantity(),
                        i.getUnitPrice(), i.getLineRefundAmount()))
                .toList();
        List<ReturnStatusHistoryResponse> history = r.getStatusHistory().stream()
                .map(h -> new ReturnStatusHistoryResponse(h.getFromStatus(), h.getToStatus(),
                        h.getNote(), h.getChangedBy(), h.getCreatedAt()))
                .toList();
        return new ReturnResponse(
                r.getId(), r.getRmaNumber(), r.getOrderId(), r.getUserId(),
                r.getStatus(), r.getReason(), r.getReasonDetails(),
                r.getRefundAmount(), r.getRefundPaymentId(),
                r.getPickupTrackingNumber(), r.getPickupScheduledAt(),
                r.getQcNote(), r.getRejectionReason(),
                items, history, r.getCreatedAt()
        );
    }

    private static Map<ReturnStatus, Set<ReturnStatus>> transitions() {
        Map<ReturnStatus, Set<ReturnStatus>> m = new EnumMap<>(ReturnStatus.class);
        m.put(ReturnStatus.REQUESTED, Set.of(ReturnStatus.APPROVED, ReturnStatus.REJECTED));
        m.put(ReturnStatus.APPROVED, Set.of(ReturnStatus.PICKUP_SCHEDULED, ReturnStatus.CLOSED));
        m.put(ReturnStatus.REJECTED, Set.of(ReturnStatus.CLOSED));
        m.put(ReturnStatus.PICKUP_SCHEDULED, Set.of(ReturnStatus.PICKED_UP, ReturnStatus.CLOSED));
        m.put(ReturnStatus.PICKED_UP, Set.of(ReturnStatus.QC_PENDING, ReturnStatus.QC_PASSED, ReturnStatus.QC_FAILED));
        m.put(ReturnStatus.QC_PENDING, Set.of(ReturnStatus.QC_PASSED, ReturnStatus.QC_FAILED));
        m.put(ReturnStatus.QC_PASSED, Set.of(ReturnStatus.REFUND_INITIATED));
        m.put(ReturnStatus.QC_FAILED, Set.of(ReturnStatus.CLOSED));
        m.put(ReturnStatus.REFUND_INITIATED, Set.of(ReturnStatus.REFUNDED, ReturnStatus.QC_PASSED));
        m.put(ReturnStatus.REFUNDED, Set.of(ReturnStatus.CLOSED));
        m.put(ReturnStatus.CLOSED, Set.of());
        return m;
    }
}
