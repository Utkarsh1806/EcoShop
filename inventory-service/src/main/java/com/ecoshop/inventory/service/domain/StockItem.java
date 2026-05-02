package com.ecoshop.inventory.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "stock_items", indexes = {
        @Index(name = "idx_stock_sku", columnList = "sku"),
        @Index(name = "idx_stock_product", columnList = "product_id"),
        @Index(name = "idx_stock_warehouse_sku", columnList = "warehouse_id,sku", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StockItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(nullable = false, length = 100)
    private String sku;

    /** Total physical units in this warehouse */
    @Column(name = "on_hand", nullable = false)
    @Builder.Default
    private Integer onHand = 0;

    /** Units reserved by pending orders/checkouts (not yet shipped) */
    @Column(name = "reserved", nullable = false)
    @Builder.Default
    private Integer reserved = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    /** Available = on_hand - reserved */
    public int available() {
        return Math.max(0, onHand - reserved);
    }
}
