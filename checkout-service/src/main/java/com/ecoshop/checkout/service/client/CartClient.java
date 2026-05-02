package com.ecoshop.checkout.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "cart-service", path = "/api/cart")
public interface CartClient {

    @GetMapping("/internal/{cartKey}")
    ApiResponse<CartView> getCart(@PathVariable("cartKey") String cartKey);

    @DeleteMapping
    ApiResponse<Void> clear(@RequestHeader("X-Cart-Key") String cartKey);

    record CartView(
            String cartKey,
            UUID userId,
            List<CartItemView> items,
            String couponCode,
            String currency,
            BigDecimal subtotal,
            int itemCount
    ) {}

    record CartItemView(
            UUID productId,
            UUID variantId,
            String sku,
            String name,
            String thumbnailUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {}
}
