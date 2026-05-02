package com.ecoshop.fraud.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.fraud.service.dto.FraudDtos.*;
import com.ecoshop.fraud.service.service.FraudEngineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudEngineService fraudService;

    /** Internal — called by checkout-service or order-service before authorizing payment */
    @PostMapping("/evaluate/order")
    public ApiResponse<FraudDecisionResponse> evaluateOrder(@Valid @RequestBody EvaluateOrderRequest req) {
        return ApiResponse.ok(fraudService.evaluateOrder(req));
    }

    @GetMapping("/checks/order/{orderId}/latest")
    public ApiResponse<FraudDecisionResponse> getLatestForOrder(@PathVariable UUID orderId) {
        return ApiResponse.ok(fraudService.getLatestForOrder(orderId));
    }

    // ─── Blocklist (admin) ───
    @PostMapping("/admin/blocklist")
    public ApiResponse<BlocklistEntryResponse> addToBlocklist(
            @RequestHeader(value = "X-Actor", defaultValue = "admin") String actor,
            @Valid @RequestBody BlocklistEntryRequest req) {
        return ApiResponse.ok(fraudService.addToBlocklist(req, actor));
    }

    @DeleteMapping("/admin/blocklist/{id}")
    public ApiResponse<Void> removeFromBlocklist(@PathVariable UUID id) {
        fraudService.removeFromBlocklist(id);
        return ApiResponse.ok(null);
    }
}
