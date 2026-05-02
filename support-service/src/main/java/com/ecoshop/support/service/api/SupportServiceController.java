package com.ecoshop.support.service.api;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Tickets, chat, bot escalation
 *
 * <p>Stub controller — replace with real endpoints as you flesh out this service.
 * The endpoints below describe the public contract this service is expected to expose.
 */
@RestController
@RequestMapping("/api/support")
public class SupportServiceController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of(
                "service", "support-service",
                "status", "UP"
        ));
    }

    @GetMapping("/contract")
    public ApiResponse<Map<String, Object>> contract() {
        return ApiResponse.ok(Map.of(
                "service", "support-service",
                "description", "Tickets, chat, bot escalation",
                "todo", "Implement domain endpoints — see README for the feature list this service owns."
        ));
    }
}
