package com.ecoshop.shipping.service.courier;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Port for courier integrations. Real implementations: Delhivery, BlueDart, Shiprocket, FedEx.
 */
public interface Courier {

    String name();

    /** Estimate the cost and delivery time for a shipment. */
    RateQuote getRate(String fromPincode, String toPincode, int weightGrams);

    /** Book the shipment, get a tracking number and label URL. */
    BookingResult book(BookingRequest req);

    /** Cancel a booking (best-effort — couriers often charge for cancellations after pickup). */
    void cancel(String trackingNumber);

    record RateQuote(BigDecimal cost, Instant estimatedDelivery, int transitDays) {}

    record BookingRequest(
            String orderId,
            String fromPincode,
            String toPincode,
            int weightGrams,
            String recipientName,
            String recipientPhone,
            BigDecimal codAmount     // null for prepaid orders
    ) {}

    record BookingResult(
            String trackingNumber,
            String labelUrl,
            BigDecimal cost,
            Instant estimatedDelivery
    ) {}
}
