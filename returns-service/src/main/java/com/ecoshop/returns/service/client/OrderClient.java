package com.ecoshop.returns.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "order-service", path = "/api/orders")
public interface OrderClient {

    @GetMapping("/{orderId}")
    ApiResponse<OrderView> getById(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable("orderId") UUID orderId);

    record OrderItemView(
            UUID id, UUID productId, UUID variantId, String sku, String productName,
            String thumbnailUrl, BigDecimal unitPrice, int quantity, BigDecimal lineTotal
    ) {}

    record OrderView(
            UUID id,
            String orderNumber,
            UUID userId,
            String status,
            BigDecimal totalAmount,
            String currency,
            String paymentId,
            List<OrderItemView> items
    ) {}
}
