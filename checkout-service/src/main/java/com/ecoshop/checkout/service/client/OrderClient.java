package com.ecoshop.checkout.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "order-service", path = "/api/orders")
public interface OrderClient {

    /**
     * Place an order. The Authorization header is forwarded so order-service treats this
     * as the user's own request (and reads userId from the JWT principal).
     */
    @PostMapping
    ApiResponse<OrderResponse> placeOrder(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody PlaceOrderRequest req);

    record AddressDto(String recipientName, String phone, String line1, String line2,
                      String city, String state, String postalCode, String country) {}

    record OrderItemDto(UUID productId, UUID variantId, String sku, String productName,
                        String thumbnailUrl, BigDecimal unitPrice, int quantity) {}

    record PlaceOrderRequest(
            List<OrderItemDto> items,
            AddressDto shippingAddress,
            String couponCode,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal shippingCost,
            String currency
    ) {}

    record OrderResponse(
            UUID id, String orderNumber, UUID userId, String status,
            BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal shippingCost,
            BigDecimal totalAmount, String currency, String couponCode,
            String paymentId, String paymentStatus,
            Object shippingAddress, Object items,
            Instant createdAt, Instant updatedAt
    ) {}
}
