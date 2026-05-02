package com.ecoshop.checkout.service.api;

import com.ecoshop.checkout.service.dto.CheckoutDtos.*;
import com.ecoshop.checkout.service.service.CheckoutOrchestrator;
import com.ecoshop.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutOrchestrator orchestrator;

    /**
     * Start a checkout. Orchestrates: cart load → availability → reserve → quote → rate →
     * place order → redeem → create payment intent. Returns clientSecret/checkoutUrl that
     * the frontend uses to complete the gateway-side flow.
     */
    @PostMapping
    public ApiResponse<CheckoutResponse> start(@AuthenticationPrincipal String userId,
                                               @RequestHeader("Authorization") String authHeader,
                                               @Valid @RequestBody StartCheckoutRequest req) {
        return ApiResponse.ok(orchestrator.start(UUID.fromString(userId), authHeader, req));
    }

    @GetMapping("/{id}")
    public ApiResponse<CheckoutResponse> get(@AuthenticationPrincipal String userId,
                                             @PathVariable UUID id) {
        return ApiResponse.ok(orchestrator.get(id, UUID.fromString(userId)));
    }
}
