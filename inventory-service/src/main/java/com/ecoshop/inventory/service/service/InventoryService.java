package com.ecoshop.inventory.service.service;

import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.inventory.service.domain.*;
import com.ecoshop.inventory.service.dto.InventoryDtos.*;
import com.ecoshop.inventory.service.repo.ReservationRepository;
import com.ecoshop.inventory.service.repo.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final StockItemRepository stockRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public AvailabilityCheckResponse checkAvailability(AvailabilityCheckRequest req) {
        List<AvailabilityResultDto> details = new ArrayList<>();
        boolean allAvailable = true;
        for (ReserveItemDto item : req.items()) {
            int total = stockRepository.findBySku(item.sku()).stream()
                    .mapToInt(StockItem::available).sum();
            boolean ok = total >= item.quantity();
            if (!ok) allAvailable = false;
            details.add(new AvailabilityResultDto(item.sku(), item.quantity(), total, ok));
        }
        return new AvailabilityCheckResponse(allAvailable, details);
    }

    /**
     * Reserve stock for a reference (orderId or checkoutId). Idempotent: re-using the same
     * referenceId returns the existing reservation rather than creating a duplicate.
     */
    @Transactional
    public ReservationResponse reserve(ReserveRequest req) {
        // Idempotency
        return reservationRepository.findByReferenceId(req.referenceId())
                .map(this::toResponse)
                .orElseGet(() -> createReservation(req));
    }

    private ReservationResponse createReservation(ReserveRequest req) {
        Reservation reservation = Reservation.builder()
                .referenceId(req.referenceId())
                .referenceType(req.referenceType())
                .status(ReservationStatus.HELD)
                .expiresAt(req.ttlSeconds() != null ? Instant.now().plusSeconds(req.ttlSeconds()) : null)
                .build();

        for (ReserveItemDto item : req.items()) {
            int remaining = item.quantity();
            // Find available stock items, take pessimistic lock, decrement
            List<StockItem> candidates = stockRepository.findAvailableBySku(item.sku());
            if (candidates.isEmpty()) {
                throw BusinessException.conflict("OUT_OF_STOCK",
                        "No stock available for SKU " + item.sku());
            }
            for (StockItem candidate : candidates) {
                if (remaining == 0) break;
                StockItem locked = stockRepository.findByIdForUpdate(candidate.getId())
                        .orElseThrow(() -> BusinessException.notFound("STOCK_NOT_FOUND",
                                "Stock disappeared during lock"));
                int take = Math.min(remaining, locked.available());
                if (take == 0) continue;
                locked.setReserved(locked.getReserved() + take);
                stockRepository.save(locked);

                reservation.addItem(ReservationItem.builder()
                        .stockItemId(locked.getId())
                        .sku(item.sku())
                        .quantity(take)
                        .build());
                remaining -= take;
            }
            if (remaining > 0) {
                throw BusinessException.conflict("INSUFFICIENT_STOCK",
                        "Cannot reserve " + item.quantity() + " of SKU " + item.sku() +
                        " — only " + (item.quantity() - remaining) + " available");
            }
        }

        Reservation saved = reservationRepository.save(reservation);
        log.info("Created reservation {} for ref {} ({} items)",
                saved.getId(), saved.getReferenceId(), saved.getItems().size());
        return toResponse(saved);
    }

    /** Convert HELD → COMMITTED. Stock stays reserved until shipment. */
    @Transactional
    public ReservationResponse commit(String referenceId) {
        Reservation r = findReservation(referenceId);
        if (r.getStatus() == ReservationStatus.COMMITTED) return toResponse(r);
        if (r.getStatus() != ReservationStatus.HELD) {
            throw BusinessException.badRequest("INVALID_RESERVATION_STATE",
                    "Cannot commit reservation in status " + r.getStatus());
        }
        r.setStatus(ReservationStatus.COMMITTED);
        return toResponse(r);
    }

    /** Release reserved units back to availability. Used on cancel/timeout/payment-failure. */
    @Transactional
    public ReservationResponse release(String referenceId) {
        Reservation r = findReservation(referenceId);
        if (r.getStatus() == ReservationStatus.RELEASED) return toResponse(r);
        if (r.getStatus() == ReservationStatus.FULFILLED) {
            throw BusinessException.badRequest("ALREADY_FULFILLED",
                    "Cannot release fulfilled reservation");
        }
        for (ReservationItem ri : r.getItems()) {
            StockItem locked = stockRepository.findByIdForUpdate(ri.getStockItemId())
                    .orElseThrow(() -> BusinessException.notFound("STOCK_NOT_FOUND", "stock missing"));
            locked.setReserved(Math.max(0, locked.getReserved() - ri.getQuantity()));
            stockRepository.save(locked);
        }
        r.setStatus(ReservationStatus.RELEASED);
        log.info("Released reservation {} for ref {}", r.getId(), referenceId);
        return toResponse(r);
    }

    /** Mark fulfilled (after shipment). Decrements on_hand and clears reservation. */
    @Transactional
    public ReservationResponse fulfill(String referenceId) {
        Reservation r = findReservation(referenceId);
        if (r.getStatus() != ReservationStatus.COMMITTED) {
            throw BusinessException.badRequest("INVALID_RESERVATION_STATE",
                    "Cannot fulfill reservation in status " + r.getStatus());
        }
        for (ReservationItem ri : r.getItems()) {
            StockItem locked = stockRepository.findByIdForUpdate(ri.getStockItemId())
                    .orElseThrow(() -> BusinessException.notFound("STOCK_NOT_FOUND", "stock missing"));
            locked.setOnHand(Math.max(0, locked.getOnHand() - ri.getQuantity()));
            locked.setReserved(Math.max(0, locked.getReserved() - ri.getQuantity()));
            stockRepository.save(locked);
        }
        r.setStatus(ReservationStatus.FULFILLED);
        log.info("Fulfilled reservation {} for ref {}", r.getId(), referenceId);
        return toResponse(r);
    }

    @Transactional
    public StockItemResponse adjustStock(AdjustStockRequest req) {
        List<StockItem> items = stockRepository.findBySku(req.sku()).stream()
                .filter(s -> s.getWarehouseId().equals(req.warehouseId()))
                .toList();
        StockItem item;
        if (items.isEmpty()) {
            if (req.delta() <= 0) {
                throw BusinessException.notFound("STOCK_NOT_FOUND",
                        "No stock for SKU " + req.sku() + " at warehouse — cannot decrement non-existent stock");
            }
            // First-time stocking: needs productId. Real flow: receive a PO and create stock.
            throw BusinessException.badRequest("STOCK_NOT_INITIALIZED",
                    "Stock for SKU " + req.sku() + " must be created first via /api/inventory/stock");
        } else {
            item = stockRepository.findByIdForUpdate(items.get(0).getId())
                    .orElseThrow(() -> BusinessException.notFound("STOCK_NOT_FOUND", "stock missing"));
        }
        int newOnHand = item.getOnHand() + req.delta();
        if (newOnHand < item.getReserved()) {
            throw BusinessException.conflict("WOULD_OVERSELL",
                    "Cannot reduce on_hand below reserved (" + item.getReserved() + ")");
        }
        item.setOnHand(newOnHand);
        log.info("Adjusted stock SKU={} warehouse={} delta={} reason={}",
                req.sku(), req.warehouseId(), req.delta(), req.reason());
        return toStockResponse(item);
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> listStockBySku(String sku) {
        return stockRepository.findBySku(sku).stream().map(this::toStockResponse).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getAvailabilityMap(List<String> skus) {
        Map<String, Integer> map = skus.stream().collect(Collectors.toMap(s -> s, s -> 0, (a, b) -> a));
        for (String sku : skus) {
            int total = stockRepository.findBySku(sku).stream().mapToInt(StockItem::available).sum();
            map.put(sku, total);
        }
        return map;
    }

    private Reservation findReservation(String referenceId) {
        return reservationRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> BusinessException.notFound("RESERVATION_NOT_FOUND",
                        "No reservation for ref " + referenceId));
    }

    public ReservationResponse toResponse(Reservation r) {
        List<ReservationItemResponse> items = r.getItems().stream()
                .map(i -> new ReservationItemResponse(i.getStockItemId(), i.getSku(), i.getQuantity()))
                .toList();
        return new ReservationResponse(
                r.getId(), r.getReferenceId(), r.getReferenceType(),
                r.getStatus(), r.getExpiresAt(), items
        );
    }

    public StockItemResponse toStockResponse(StockItem s) {
        return new StockItemResponse(
                s.getId(), s.getWarehouseId(), s.getProductId(), s.getVariantId(),
                s.getSku(), s.getOnHand(), s.getReserved(), s.available(), s.getLowStockThreshold()
        );
    }
}
