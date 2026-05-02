package com.ecoshop.inventory.service.dto;

import com.ecoshop.inventory.service.domain.ReservationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class InventoryDtos {

    public record StockItemResponse(
            UUID id, UUID warehouseId, UUID productId, UUID variantId,
            String sku, int onHand, int reserved, int available, Integer lowStockThreshold
    ) {}

    public record AdjustStockRequest(
            @NotBlank String sku,
            @NotNull UUID warehouseId,
            int delta,                  // positive = restock, negative = correction
            String reason
    ) {}

    public record ReserveItemDto(
            @NotBlank String sku,
            @Min(1) int quantity
    ) {}

    public record ReserveRequest(
            @NotBlank String referenceId,
            @NotBlank String referenceType,        // ORDER | CHECKOUT
            @NotEmpty @Valid List<ReserveItemDto> items,
            Long ttlSeconds                        // null → permanent until released/committed
    ) {}

    public record ReservationItemResponse(
            UUID stockItemId, String sku, int quantity
    ) {}

    public record ReservationResponse(
            UUID id,
            String referenceId,
            String referenceType,
            ReservationStatus status,
            Instant expiresAt,
            List<ReservationItemResponse> items
    ) {}

    public record AvailabilityCheckRequest(
            @NotEmpty @Valid List<ReserveItemDto> items
    ) {}

    public record AvailabilityResultDto(
            String sku,
            int requested,
            int available,
            boolean sufficient
    ) {}

    public record AvailabilityCheckResponse(
            boolean allAvailable,
            List<AvailabilityResultDto> details
    ) {}
}
