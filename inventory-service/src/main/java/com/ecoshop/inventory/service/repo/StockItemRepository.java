package com.ecoshop.inventory.service.repo;

import com.ecoshop.inventory.service.domain.StockItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    List<StockItem> findBySku(String sku);
    List<StockItem> findByProductId(UUID productId);

    /** Find first available stock for a SKU across warehouses (simple — picks any with availability). */
    @Query("SELECT s FROM StockItem s WHERE s.sku = :sku AND (s.onHand - s.reserved) > 0 ORDER BY (s.onHand - s.reserved) DESC")
    List<StockItem> findAvailableBySku(@Param("sku") String sku);

    /** Pessimistic lock for the reserve flow — prevents two concurrent reservations from oversubscribing. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockItem s WHERE s.id = :id")
    Optional<StockItem> findByIdForUpdate(@Param("id") UUID id);
}
