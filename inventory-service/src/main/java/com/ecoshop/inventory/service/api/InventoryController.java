package com.ecoshop.inventory.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.inventory.service.dto.InventoryDtos.*;
import com.ecoshop.inventory.service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/availability")
    public ApiResponse<AvailabilityCheckResponse> checkAvailability(
            @Valid @RequestBody AvailabilityCheckRequest req) {
        return ApiResponse.ok(inventoryService.checkAvailability(req));
    }

    @PostMapping("/availability/skus")
    public ApiResponse<Map<String, Integer>> getAvailabilityMap(@RequestBody List<String> skus) {
        return ApiResponse.ok(inventoryService.getAvailabilityMap(skus));
    }

    @PostMapping("/reservations")
    public ApiResponse<ReservationResponse> reserve(@Valid @RequestBody ReserveRequest req) {
        return ApiResponse.ok(inventoryService.reserve(req));
    }

    @PostMapping("/reservations/{referenceId}/commit")
    public ApiResponse<ReservationResponse> commit(@PathVariable String referenceId) {
        return ApiResponse.ok(inventoryService.commit(referenceId));
    }

    @PostMapping("/reservations/{referenceId}/release")
    public ApiResponse<ReservationResponse> release(@PathVariable String referenceId) {
        return ApiResponse.ok(inventoryService.release(referenceId));
    }

    @PostMapping("/reservations/{referenceId}/fulfill")
    public ApiResponse<ReservationResponse> fulfill(@PathVariable String referenceId) {
        return ApiResponse.ok(inventoryService.fulfill(referenceId));
    }

    @GetMapping("/stock/sku/{sku}")
    public ApiResponse<List<StockItemResponse>> getStockBySku(@PathVariable String sku) {
        return ApiResponse.ok(inventoryService.listStockBySku(sku));
    }

    @PostMapping("/stock/adjust")
    public ApiResponse<StockItemResponse> adjustStock(@Valid @RequestBody AdjustStockRequest req) {
        return ApiResponse.ok(inventoryService.adjustStock(req));
    }
}
