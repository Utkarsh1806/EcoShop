package com.ecoshop.product.catalog.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_slug", columnList = "slug", unique = true),
        @Index(name = "idx_products_category", columnList = "category_id"),
        @Index(name = "idx_products_brand", columnList = "brand_id"),
        @Index(name = "idx_products_status", columnList = "status")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(nullable = false, unique = true, length = 300)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "brand_id")
    private UUID brandId;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    public void addVariant(ProductVariant v) { variants.add(v); v.setProduct(this); }
    public void addImage(ProductImage i) { images.add(i); i.setProduct(this); }
}
