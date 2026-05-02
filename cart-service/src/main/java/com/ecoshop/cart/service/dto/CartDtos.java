package com.ecoshop.cart.service.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CartDtos {

    public record AddItemRequest(
            @NotNull UUID productId,
            UUID variantId,
            @Min(1) @Max(100) int quantity
    ) {}

    public record UpdateQuantityRequest(
            @Min(0) @Max(100) int quantity
    ) {}

    public record CartItemResponse(
            UUID productId,
            UUID variantId,
            String sku,
            String name,
            String thumbnailUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {}

    public record CartResponse(
            String cartKey,
            UUID userId,
            List<CartItemResponse> items,
            String couponCode,
            String currency,
            BigDecimal subtotal,
            int itemCount
    ) {}

    public record ApplyCouponRequest(
            @NotBlank String code
    ) {}
}
