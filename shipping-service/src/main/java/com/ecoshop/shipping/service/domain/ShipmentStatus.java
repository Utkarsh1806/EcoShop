package com.ecoshop.shipping.service.domain;

public enum ShipmentStatus {
    CREATED,
    LABEL_GENERATED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    UNDELIVERED,
    RETURN_IN_TRANSIT,
    RETURNED_TO_ORIGIN,
    CANCELLED
}
