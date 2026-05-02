package com.ecoshop.recommendation.service.api;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Personalized recommendations (ML contract)
 *
 * <p>Stub controller — replace with real endpoints as you flesh out this service.
 * The endpoints below describe the public contract this service is expected to expose.
 */
@RestController
@RequestMapping("/api/recommendation")
public class RecommendationServiceController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of(
                "service", "recommendation-service",
                "status", "UP"
        ));
    }

    @GetMapping("/contract")
    public ApiResponse<Map<String, Object>> contract() {
        return ApiResponse.ok(Map.of(
                "service", "recommendation-service",
                "description", "Personalized recommendations (ML contract)",
                "todo", "Implement domain endpoints — see README for the feature list this service owns."
        ));
    }
}
