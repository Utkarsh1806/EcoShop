package com.ecoshop.product.catalog.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variants_sku", columnList = "sku", unique = true),
        @Index(name = "idx_variants_product", columnList = "product_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductVariant extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(length = 100)
    private String size;

    @Column(length = 50)
    private String color;

    @Column(length = 100)
    private String storage;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price; // overrides base_price if set

    @Column(name = "compare_at_price", precision = 12, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
