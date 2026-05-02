package com.ecoshop.order.service.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PACKED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    RETURNED,
    REFUNDED,
    FAILED
}
