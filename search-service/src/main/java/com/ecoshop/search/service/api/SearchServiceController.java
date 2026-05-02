package com.ecoshop.search.service.api;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Search & discovery (Elasticsearch contract)
 *
 * <p>Stub controller — replace with real endpoints as you flesh out this service.
 * The endpoints below describe the public contract this service is expected to expose.
 */
@RestController
@RequestMapping("/api/search")
public class SearchServiceController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of(
                "service", "search-service",
                "status", "UP"
        ));
    }

    @GetMapping("/contract")
    public ApiResponse<Map<String, Object>> contract() {
        return ApiResponse.ok(Map.of(
                "service", "search-service",
                "description", "Search & discovery (Elasticsearch contract)",
                "todo", "Implement domain endpoints — see README for the feature list this service owns."
        ));
    }
}
