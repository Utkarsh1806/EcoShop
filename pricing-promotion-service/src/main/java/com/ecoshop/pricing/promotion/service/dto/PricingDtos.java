package com.ecoshop.pricing.promotion.service.dto;

import com.ecoshop.pricing.promotion.service.domain.DiscountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PricingDtos {

    public record CouponRequest(
            @NotBlank @Size(max = 50) String code,
            @Size(max = 200) String description,
            @NotNull DiscountType discountType,
            @NotNull @DecimalMin("0.01") BigDecimal discountValue,
            @DecimalMin("0.0") BigDecimal maxDiscountAmount,
            @DecimalMin("0.0") BigDecimal minCartAmount,
            @NotNull Instant validFrom,
            @NotNull Instant validUntil,
            Integer usageLimit,
            Integer perUserLimit
    ) {}

    public record CouponResponse(
            UUID id, String code, String description,
            DiscountType discountType, BigDecimal discountValue,
            BigDecimal maxDiscountAmount, BigDecimal minCartAmount,
            Instant validFrom, Instant validUntil,
            Integer usageLimit, Integer usageCount, Integer perUserLimit,
            boolean active
    ) {}

    public record QuoteRequest(
            @NotBlank String code,
            @NotNull UUID userId,
            @NotNull @DecimalMin("0.0") BigDecimal cartSubtotal
    ) {}

    public record QuoteResponse(
            String code,
            boolean valid,
            BigDecimal discountAmount,
            BigDecimal subtotalAfterDiscount,
            String message
    ) {}

    public record RedeemRequest(
            @NotBlank String code,
            @NotNull UUID userId,
            @NotNull UUID orderId,
            @NotNull @DecimalMin("0.0") BigDecimal cartSubtotal
    ) {}

    public record RedeemResponse(
            UUID redemptionId,
            String code,
            BigDecimal discountApplied
    ) {}
}
