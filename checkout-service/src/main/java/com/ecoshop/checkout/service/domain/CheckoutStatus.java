package com.ecoshop.checkout.service.domain;

public enum CheckoutStatus {
    STARTED,
    STOCK_RESERVED,
    PRICED,
    ORDER_PLACED,
    PAYMENT_PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}
