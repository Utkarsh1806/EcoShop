package com.ecoshop.shipping.service.dto;

import com.ecoshop.shipping.service.domain.ShipmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ShippingDtos {

    public record AddressDto(
            @NotBlank @Size(max = 200) String recipientName,
            @NotBlank @Size(max = 20) String phone,
            @NotBlank @Size(max = 255) String line1,
            String line2,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(max = 100) String state,
            @NotBlank @Size(max = 20) String postalCode,
            @Size(min = 2, max = 2) String country
    ) {}

    public record RateQuoteRequest(
            @NotBlank String fromPincode,
            @NotBlank String toPincode,
            @Min(1) int weightGrams
    ) {}

    public record RateQuoteResponse(
            String courier,
            BigDecimal cost,
            Instant estimatedDelivery,
            int transitDays
    ) {}

    public record CreateShipmentRequest(
            @NotNull UUID orderId,
            @NotNull UUID userId,
            @NotNull @Valid AddressDto destination,
            @Min(1) int weightGrams,
            String fromPincode,
            BigDecimal codAmount
    ) {}

    public record TrackingEventResponse(
            ShipmentStatus status,
            Instant occurredAt,
            String location,
            String description
    ) {}

    public record ShipmentResponse(
            UUID id,
            UUID orderId,
            UUID userId,
            String courier,
            String trackingNumber,
            String labelUrl,
            ShipmentStatus status,
            BigDecimal shippingCost,
            Integer weightGrams,
            Instant estimatedDelivery,
            Instant deliveredAt,
            AddressDto destination,
            List<TrackingEventResponse> trackingEvents
    ) {}

    public record UpdateStatusRequest(
            @NotNull ShipmentStatus status,
            String location,
            String description
    ) {}
}
