package com.ecoshop.checkout.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "pricing-promotion-service", path = "/api/pricing")
public interface PricingClient {

    @PostMapping("/coupons/quote")
    ApiResponse<QuoteResponse> quote(@RequestBody QuoteRequest req);

    @PostMapping("/coupons/redeem")
    ApiResponse<RedeemResponse> redeem(@RequestBody RedeemRequest req);

    record QuoteRequest(String code, UUID userId, BigDecimal cartSubtotal) {}
    record QuoteResponse(String code, boolean valid, BigDecimal discountAmount,
                         BigDecimal subtotalAfterDiscount, String message) {}

    record RedeemRequest(String code, UUID userId, UUID orderId, BigDecimal cartSubtotal) {}
    record RedeemResponse(UUID redemptionId, String code, BigDecimal discountApplied) {}
}
