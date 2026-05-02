package com.ecoshop.product.catalog.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_images", indexes = {
        @Index(name = "idx_images_product", columnList = "product_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(length = 200)
    private String altText;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
