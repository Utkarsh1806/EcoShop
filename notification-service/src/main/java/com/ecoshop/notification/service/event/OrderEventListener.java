package com.ecoshop.notification.service.event;

import com.ecoshop.notification.service.domain.Channel;
import com.ecoshop.notification.service.dto.NotificationDtos.SendRequest;
import com.ecoshop.notification.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Reacts to lifecycle events emitted by other services. For now we render notifications inline
 * with simple string templates; in production these should be Freemarker/Thymeleaf templates
 * rendered with template-key + locale, and the recipient should be fetched from user-service
 * via Feign rather than hardcoded.
 */
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final NotificationService notificationService;

    @KafkaListener(topics = "order.created", groupId = "notification-service")
    public void onOrderCreated(Map<String, Object> evt) {
        try {
            String orderId = String.valueOf(evt.get("orderId"));
            String orderNumber = String.valueOf(evt.get("orderNumber"));
            String userId = String.valueOf(evt.get("userId"));
            String total = String.valueOf(evt.get("totalAmount"));
            String currency = String.valueOf(evt.get("currency"));

            // In production: Feign call to user-service to fetch email/phone/preferences.
            // For now we demonstrate the flow with a placeholder recipient.
            String recipient = "user-" + userId + "@ecoshop.local";
            String subject = "Order " + orderNumber + " received";
            String body = "Thanks for your order " + orderNumber + ".\n" +
                          "Total: " + currency + " " + total + ".\n" +
                          "We'll notify you when it ships.";

            notificationService.send(new SendRequest(
                    UUID.fromString(userId),
                    Channel.EMAIL,
                    recipient,
                    subject,
                    body,
                    "order_created",
                    "order_created:" + orderId
            ));
        } catch (Exception e) {
            log.error("Failed to handle order.created: {}", evt, e);
        }
    }

    @KafkaListener(topics = "payment.succeeded", groupId = "notification-service")
    public void onPaymentSucceeded(Map<String, Object> evt) {
        try {
            String orderId = String.valueOf(evt.get("orderId"));
            String paymentId = String.valueOf(evt.get("paymentId"));
            String amount = String.valueOf(evt.get("amount"));
            String currency = String.valueOf(evt.get("currency"));

            // No userId in payment events as we have them — would require a lookup.
            // The dedupe key + per-orderId lookup ensures we don't double-send.
            notificationService.send(new SendRequest(
                    null,
                    Channel.EMAIL,
                    "order-" + orderId + "@ecoshop.local",
                    "Payment received",
                    "Your payment of " + currency + " " + amount + " has been received. " +
                            "Order will be shipped shortly. (Payment ID: " + paymentId + ")",
                    "payment_succeeded",
                    "payment_succeeded:" + paymentId
            ));
        } catch (Exception e) {
            log.error("Failed to handle payment.succeeded: {}", evt, e);
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service")
    public void onPaymentFailed(Map<String, Object> evt) {
        try {
            String orderId = String.valueOf(evt.get("orderId"));
            String reason = String.valueOf(evt.getOrDefault("reason", "Unknown reason"));

            notificationService.send(new SendRequest(
                    null,
                    Channel.EMAIL,
                    "order-" + orderId + "@ecoshop.local",
                    "Payment failed",
                    "We couldn't process your payment for order " + orderId + ". Reason: " + reason +
                            ". Please retry from your orders page.",
                    "payment_failed",
                    "payment_failed:" + orderId + ":" + System.currentTimeMillis() / 86_400_000  // dedupe per-day
            ));
        } catch (Exception e) {
            log.error("Failed to handle payment.failed: {}", evt, e);
        }
    }

    @KafkaListener(topics = "order.status.changed", groupId = "notification-service")
    public void onOrderStatusChanged(Map<String, Object> evt) {
        try {
            String orderId = String.valueOf(evt.get("orderId"));
            String orderNumber = String.valueOf(evt.get("orderNumber"));
            String toStatus = String.valueOf(evt.get("toStatus"));

            // Only send notifications for user-relevant statuses
            String subject;
            String body;
            switch (toStatus) {
                case "PACKED" -> {
                    subject = "Order " + orderNumber + " packed";
                    body = "Good news — your order is packed and will be shipped soon.";
                }
                case "SHIPPED" -> {
                    subject = "Order " + orderNumber + " shipped";
                    body = "Your order is on its way. You'll receive tracking details shortly.";
                }
                case "OUT_FOR_DELIVERY" -> {
                    subject = "Out for delivery";
                    body = "Your order " + orderNumber + " is out for delivery today.";
                }
                case "DELIVERED" -> {
                    subject = "Order " + orderNumber + " delivered";
                    body = "Your order has been delivered. Hope you love it!";
                }
                case "CANCELLED" -> {
                    subject = "Order " + orderNumber + " cancelled";
                    body = "Your order has been cancelled. Any payment will be refunded within 5–7 business days.";
                }
                default -> {
                    return; // not a user-facing status change
                }
            }
            notificationService.send(new SendRequest(
                    null,
                    Channel.EMAIL,
                    "order-" + orderId + "@ecoshop.local",
                    subject, body,
                    "order_status_" + toStatus.toLowerCase(),
                    "order_status:" + orderId + ":" + toStatus
            ));
        } catch (Exception e) {
            log.error("Failed to handle order.status.changed: {}", evt, e);
        }
    }
}
