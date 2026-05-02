package com.ecoshop.inventory.service.domain;

public enum ReservationStatus {
    HELD,         // soft hold (e.g. during checkout)
    COMMITTED,    // hard hold after order placed
    RELEASED,     // released back to stock (cancel/timeout)
    FULFILLED     // shipped, decrement on_hand
}
