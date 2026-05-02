package com.ecoshop.order.service.event;

import com.ecoshop.order.service.service.OrderManagementService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Consumes payment events from payment-service and updates the order status accordingly.
 * Topics: payment.succeeded, payment.failed
 *
 * Payload shape (JSON):
 * { "orderId": "uuid", "paymentId": "pay_xxx", "reason": "..." }
 */
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final OrderManagementService orderService;

    @KafkaListener(topics = {"payment.succeeded", "payment.failed"}, groupId = "order-service")
    public void onPaymentEvent(Map<String, Object> payload,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            Object oid = payload.get("orderId");
            if (oid == null) {
                log.warn("payment event missing orderId: {}", payload);
                return;
            }
            UUID orderId = UUID.fromString(oid.toString());
            String paymentId = payload.get("paymentId") != null ? payload.get("paymentId").toString() : null;

            if ("payment.succeeded".equals(topic)) {
                orderService.markPaid(orderId, paymentId);
                log.info("Order {} → PAID (payment {})", orderId, paymentId);
            } else if ("payment.failed".equals(topic)) {
                String reason = payload.get("reason") != null ? payload.get("reason").toString() : "Payment failed";
                orderService.markFailed(orderId, reason);
                log.info("Order {} → FAILED ({})", orderId, reason);
            }
        } catch (Exception e) {
            log.error("Failed to process payment event from topic {}: {}", topic, payload, e);
            // In production: send to DLQ
        }
    }
}
