package com.ecoshop.payment.service.domain;

public enum PaymentStatus {
    CREATED,        // payment intent created, awaiting customer action
    PROCESSING,     // gateway is processing
    SUCCEEDED,      // captured
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    REFUNDED,
    PARTIALLY_REFUNDED
}
