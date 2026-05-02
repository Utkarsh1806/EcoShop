package com.ecoshop.product.catalog.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "brands", indexes = {
        @Index(name = "idx_brands_slug", columnList = "slug", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Brand extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(length = 500)
    private String logoUrl;

    @Column(length = 1000)
    private String description;
}
