package com.ecoshop.returns.service.domain;

public enum ReturnStatus {
    REQUESTED,
    APPROVED,
    REJECTED,
    PICKUP_SCHEDULED,
    PICKED_UP,
    QC_PENDING,
    QC_PASSED,
    QC_FAILED,
    REFUND_INITIATED,
    REFUNDED,
    CLOSED
}
