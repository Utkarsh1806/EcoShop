package com.ecoshop.fraud.service.event;

import com.ecoshop.fraud.service.dto.FraudDtos.EvaluateOrderRequest;
import com.ecoshop.fraud.service.service.FraudEngineService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for order.created and runs a fraud check automatically. The check publishes a
 * fraud.decision event that order-service / payment-service should consume to gate the flow.
 *
 * Note: order.created in this scaffold doesn't carry IP, device, billing pincode, etc — those
 * would come from the request context at order placement time and need to be added to the event
 * (or fetched via a Feign call to order-service's enriched view).
 */
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final FraudEngineService fraudService;

    @KafkaListener(topics = "order.created", groupId = "fraud-service")
    public void onOrderCreated(Map<String, Object> evt) {
        try {
            UUID orderId = UUID.fromString(String.valueOf(evt.get("orderId")));
            UUID userId = UUID.fromString(String.valueOf(evt.get("userId")));
            BigDecimal total = new BigDecimal(String.valueOf(evt.get("totalAmount")));
            String currency = String.valueOf(evt.getOrDefault("currency", "INR"));

            EvaluateOrderRequest req = new EvaluateOrderRequest(
                    orderId, userId,
                    null, null, null, null,        // email/phone/ip/device — not in current event payload
                    total, currency, 1,
                    null, null, null
            );
            fraudService.evaluateOrder(req);
        } catch (Exception e) {
            log.warn("Could not auto-evaluate order: {}", e.getMessage());
        }
    }
}
