package com.ecoshop.shipping.service.event;

import com.ecoshop.shipping.service.dto.ShippingDtos.AddressDto;
import com.ecoshop.shipping.service.dto.ShippingDtos.CreateShipmentRequest;
import com.ecoshop.shipping.service.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Listens for order status changes. When an order transitions to PACKED, automatically books
 * a shipment.
 *
 * <p>Note: in this scaffold the order.status.changed event doesn't carry the shipping address.
 * In production, the listener should call order-service via Feign to fetch the full order
 * (including the embedded shipping address snapshot) before booking. For now, we log and
 * skip — the controller endpoint can be used directly by checkout-service for the demo flow.
 */
@Component
@RequiredArgsConstructor
public class OrderStatusListener {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusListener.class);

    private final ShippingService shippingService;

    @KafkaListener(topics = "order.status.changed", groupId = "shipping-service")
    public void onOrderStatusChanged(Map<String, Object> evt) {
        try {
            String toStatus = String.valueOf(evt.get("toStatus"));
            String orderId = String.valueOf(evt.get("orderId"));
            if (!"PACKED".equals(toStatus)) return;

            log.info("Order {} packed — shipment booking should be triggered. " +
                     "Pending: Feign call to order-service to fetch shipping address.", orderId);
            // TODO: feignClient.getOrder(orderId) → extract shippingAddress → call shippingService.createShipment(...)
            // Left as an extension for Claude Code so the service-to-service contract is explicit.
        } catch (Exception e) {
            log.warn("Failed to process order.status.changed: {}", e.getMessage());
        }
    }
}
