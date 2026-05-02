package com.ecoshop.shipping.service.courier;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Mock courier for local dev. Always succeeds, generates fake tracking numbers,
 * returns cost based on weight + distance heuristic.
 */
@Component
public class MockCourier implements Courier {

    @Override
    public String name() { return "MOCK_COURIER"; }

    @Override
    public RateQuote getRate(String fromPincode, String toPincode, int weightGrams) {
        // Heuristic: ₹40 base + ₹10/100g + ₹0.5/km (estimated by pincode delta)
        BigDecimal base = BigDecimal.valueOf(40);
        BigDecimal weightCost = BigDecimal.valueOf(weightGrams).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(10));
        int pinDelta = Math.abs(parsePincode(fromPincode) - parsePincode(toPincode));
        BigDecimal distanceCost = BigDecimal.valueOf(pinDelta).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(weightCost).add(distanceCost).setScale(2, RoundingMode.HALF_UP);

        // Local delivery (same city) ~2 days, far ~5 days
        int days = pinDelta < 1000 ? 2 : pinDelta < 100_000 ? 4 : 6;
        return new RateQuote(total, Instant.now().plus(days, ChronoUnit.DAYS), days);
    }

    @Override
    public BookingResult book(BookingRequest req) {
        RateQuote rate = getRate(req.fromPincode(), req.toPincode(), req.weightGrams());
        String tracking = "MK" + System.currentTimeMillis() +
                UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String labelUrl = "https://mock-couriers.local/labels/" + tracking + ".pdf";
        return new BookingResult(tracking, labelUrl, rate.cost(), rate.estimatedDelivery());
    }

    @Override
    public void cancel(String trackingNumber) {
        // No-op for mock
    }

    private int parsePincode(String pin) {
        try {
            return Integer.parseInt(pin.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 110001; // Delhi default
        }
    }
}
