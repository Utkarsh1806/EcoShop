package com.ecoshop.checkout.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryClient {

    @PostMapping("/availability")
    ApiResponse<AvailabilityCheckResponse> checkAvailability(@RequestBody AvailabilityCheckRequest req);

    @PostMapping("/reservations")
    ApiResponse<ReservationResponse> reserve(@RequestBody ReserveRequest req);

    @PostMapping("/reservations/{referenceId}/release")
    ApiResponse<ReservationResponse> release(@PathVariable("referenceId") String referenceId);

    record ReserveItem(String sku, int quantity) {}

    record AvailabilityCheckRequest(List<ReserveItem> items) {}

    record AvailabilityResult(String sku, int requested, int available, boolean sufficient) {}

    record AvailabilityCheckResponse(boolean allAvailable, List<AvailabilityResult> details) {}

    record ReserveRequest(String referenceId, String referenceType, List<ReserveItem> items, Long ttlSeconds) {}

    record ReservationItemView(UUID stockItemId, String sku, int quantity) {}

    record ReservationResponse(
            UUID id, String referenceId, String referenceType, String status,
            Instant expiresAt, List<ReservationItemView> items
    ) {}
}
