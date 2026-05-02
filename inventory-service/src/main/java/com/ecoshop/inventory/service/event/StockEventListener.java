package com.ecoshop.inventory.service.event;

import com.ecoshop.inventory.service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Releases reservations when payment fails or order is cancelled, so stock returns to availability.
 *
 * Subscribed topics:
 *   - payment.failed   → release reservation tied to orderId
 *   - order.status.changed → if toStatus == CANCELLED, release
 */
@Component
@RequiredArgsConstructor
public class StockEventListener {

    private static final Logger log = LoggerFactory.getLogger(StockEventListener.class);

    private final InventoryService inventoryService;

    @KafkaListener(topics = "payment.failed", groupId = "inventory-service")
    public void onPaymentFailed(Map<String, Object> evt,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            Object orderId = evt.get("orderId");
            if (orderId == null) return;
            inventoryService.release(orderId.toString());
            log.info("Released reservation for order {} (payment failed)", orderId);
        } catch (Exception e) {
            log.warn("Could not release on payment.failed: {}", e.getMessage());
            // Reservation may not exist yet (e.g. checkout used a different ref) — log and move on
        }
    }

    @KafkaListener(topics = "order.status.changed", groupId = "inventory-service")
    public void onOrderStatusChanged(Map<String, Object> evt,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            String toStatus = String.valueOf(evt.get("toStatus"));
            String orderId = String.valueOf(evt.get("orderId"));
            if (orderId == null || "null".equals(orderId)) return;

            switch (toStatus) {
                case "CANCELLED" -> {
                    inventoryService.release(orderId);
                    log.info("Released reservation for order {} (cancelled)", orderId);
                }
                case "PAID" -> {
                    inventoryService.commit(orderId);
                    log.info("Committed reservation for order {} (paid)", orderId);
                }
                case "SHIPPED" -> {
                    inventoryService.fulfill(orderId);
                    log.info("Fulfilled reservation for order {} (shipped)", orderId);
                }
                default -> { /* ignore */ }
            }
        } catch (Exception e) {
            log.warn("Could not process order.status.changed: {}", e.getMessage());
        }
    }
}
