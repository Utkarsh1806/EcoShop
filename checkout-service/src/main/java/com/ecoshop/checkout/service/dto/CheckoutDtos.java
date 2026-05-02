package com.ecoshop.checkout.service.dto;

import com.ecoshop.checkout.service.domain.CheckoutStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public class CheckoutDtos {

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

    public record StartCheckoutRequest(
            @NotBlank String cartKey,
            @NotNull @Valid AddressDto shippingAddress,
            String couponCode,
            @NotBlank String paymentMethod, // UPI | CARD | NETBANKING | WALLET | COD
            @NotBlank String idempotencyKey
    ) {}

    public record CheckoutResponse(
            UUID id,
            CheckoutStatus status,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal shippingCost,
            BigDecimal totalAmount,
            String currency,
            UUID orderId,
            String orderNumber,
            UUID paymentId,
            String paymentGatewayRef,
            String clientSecret,
            String checkoutUrl,
            String failureReason
    ) {}
}
