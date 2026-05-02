package com.ecoshop.inventory.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "warehouses", indexes = {
        @Index(name = "idx_warehouses_code", columnList = "code", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Warehouse extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String city;

    @Column(length = 2)
    @Builder.Default
    private String country = "IN";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
