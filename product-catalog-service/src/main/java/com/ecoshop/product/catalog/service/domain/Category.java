package com.ecoshop.product.catalog.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_categories_slug", columnList = "slug", unique = true),
        @Index(name = "idx_categories_parent", columnList = "parent_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Category extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
