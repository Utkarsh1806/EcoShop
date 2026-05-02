package com.ecoshop.pricing.promotion.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.pricing.promotion.service.dto.PricingDtos.*;
import com.ecoshop.pricing.promotion.service.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/coupons")
    public ApiResponse<CouponResponse> create(@Valid @RequestBody CouponRequest req) {
        return ApiResponse.ok(pricingService.createCoupon(req));
    }

    @GetMapping("/coupons/{code}")
    public ApiResponse<CouponResponse> getByCode(@PathVariable String code) {
        return ApiResponse.ok(pricingService.getByCode(code));
    }

    /** Quote a discount (no side effects). Used by checkout/cart for "preview". */
    @PostMapping("/coupons/quote")
    public ApiResponse<QuoteResponse> quote(@Valid @RequestBody QuoteRequest req) {
        return ApiResponse.ok(pricingService.quote(req));
    }

    /** Redeem (idempotent on orderId). Called by checkout-service when finalizing an order. */
    @PostMapping("/coupons/redeem")
    public ApiResponse<RedeemResponse> redeem(@Valid @RequestBody RedeemRequest req) {
        return ApiResponse.ok(pricingService.redeem(req));
    }
}
