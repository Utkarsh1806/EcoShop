package com.ecoshop.order.service.dto;

import com.ecoshop.order.service.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderDtos {

    public record ShippingAddressDto(
            @NotBlank String recipientName,
            @NotBlank String phone,
            @NotBlank String line1,
            String line2,
            @NotBlank String city,
            @NotBlank String state,
            @NotBlank String postalCode,
            @Size(min = 2, max = 2) String country
    ) {}

    public record PlaceOrderItemDto(
            @NotNull UUID productId,
            UUID variantId,
            String sku,
            @NotBlank String productName,
            String thumbnailUrl,
            @NotNull @DecimalMin("0.01") BigDecimal unitPrice,
            @Min(1) int quantity
    ) {}

    public record PlaceOrderRequest(
            @NotEmpty @Valid List<PlaceOrderItemDto> items,
            @NotNull @Valid ShippingAddressDto shippingAddress,
            String couponCode,
            @DecimalMin("0.0") BigDecimal discount,
            @DecimalMin("0.0") BigDecimal tax,
            @DecimalMin("0.0") BigDecimal shippingCost,
            String currency
    ) {}

    public record OrderItemResponse(
            UUID id,
            UUID productId,
            UUID variantId,
            String sku,
            String productName,
            String thumbnailUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {}

    public record OrderResponse(
            UUID id,
            String orderNumber,
            UUID userId,
            OrderStatus status,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal shippingCost,
            BigDecimal totalAmount,
            String currency,
            String couponCode,
            String paymentId,
            String paymentStatus,
            ShippingAddressDto shippingAddress,
            List<OrderItemResponse> items,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record UpdateStatusRequest(
            @NotNull OrderStatus status,
            String note
    ) {}
}
