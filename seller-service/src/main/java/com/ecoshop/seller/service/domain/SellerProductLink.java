package com.ecoshop.seller.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Joins a product (in product-catalog-service) to its owning seller. Approval workflow:
 * a seller submits a product → admin reviews → approved products show in catalog.
 */
@Entity
@Table(name = "seller_product_links", indexes = {
        @Index(name = "idx_spl_seller", columnList = "seller_id"),
        @Index(name = "idx_spl_product", columnList = "product_id", unique = true),
        @Index(name = "idx_spl_status", columnList = "approval_status")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SellerProductLink extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "approval_status", nullable = false, length = 32)
    @Builder.Default
    private String approvalStatus = "PENDING"; // PENDING | APPROVED | REJECTED

    @Column(name = "approval_note", length = 500)
    private String approvalNote;
}
