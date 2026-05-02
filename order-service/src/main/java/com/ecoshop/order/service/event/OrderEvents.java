package com.ecoshop.order.service.event;

import com.ecoshop.common.event.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrderEvents {

    public static final String TOPIC_ORDER_CREATED = "order.created";
    public static final String TOPIC_ORDER_STATUS_CHANGED = "order.status.changed";

    public record OrderCreatedEvent(
            UUID eventId,
            Instant occurredAt,
            UUID orderId,
            String orderNumber,
            UUID userId,
            BigDecimal totalAmount,
            String currency
    ) implements DomainEvent {
        @Override public String aggregateType() { return "Order"; }
        @Override public String aggregateId() { return orderId.toString(); }
    }

    public record OrderStatusChangedEvent(
            UUID eventId,
            Instant occurredAt,
            UUID orderId,
            String orderNumber,
            String fromStatus,
            String toStatus
    ) implements DomainEvent {
        @Override public String aggregateType() { return "Order"; }
        @Override public String aggregateId() { return orderId.toString(); }
    }
}
