package com.ecoshop.payment.service.dto;

import com.ecoshop.payment.service.domain.PaymentMethod;
import com.ecoshop.payment.service.domain.PaymentStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentDtos {

    public record CreatePaymentRequest(
            @NotNull UUID orderId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @Size(min = 3, max = 3) String currency,
            @NotNull PaymentMethod method,
            @NotBlank String idempotencyKey
    ) {}

    public record PaymentResponse(
            UUID id,
            UUID orderId,
            UUID userId,
            BigDecimal amount,
            String currency,
            PaymentMethod method,
            PaymentStatus status,
            String gateway,
            String gatewayRef,
            String clientSecret,
            String checkoutUrl
    ) {}

    public record CapturePaymentRequest(
            @NotBlank String gatewayRef
    ) {}

    public record RefundRequest(
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            String reason
    ) {}

    public record RefundResponse(
            UUID paymentId,
            BigDecimal amountRefunded,
            PaymentStatus status,
            String message
    ) {}
}
