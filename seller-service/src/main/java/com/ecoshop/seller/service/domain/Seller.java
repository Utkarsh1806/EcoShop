package com.ecoshop.seller.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sellers", indexes = {
        @Index(name = "idx_sellers_user", columnList = "user_id", unique = true),
        @Index(name = "idx_sellers_legal_name", columnList = "legal_name"),
        @Index(name = "idx_sellers_gstin", columnList = "gstin", unique = true),
        @Index(name = "idx_sellers_status", columnList = "status")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Seller extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Owning user account in user-service. One user → one seller account. */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "legal_name", nullable = false, length = 300)
    private String legalName;

    @Column(name = "contact_email", nullable = false, length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /** Indian-specific identifiers */
    @Column(name = "gstin", unique = true, length = 15)
    private String gstin;

    @Column(name = "pan", length = 10)
    private String pan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private SellerStatus status = SellerStatus.PENDING_VERIFICATION;

    /** Commission rate as a fraction (e.g. 0.10 = 10%). */
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal commissionRate = new BigDecimal("0.1000");

    /** Bank account snapshot — for production, isolate to a separate vault-encrypted table. */
    @Column(name = "bank_account_holder", length = 200)
    private String bankAccountHolder;

    @Column(name = "bank_account_last4", length = 4)
    private String bankAccountLast4; // store only last 4 digits in this stub

    @Column(name = "bank_ifsc", length = 11)
    private String bankIfsc;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
}
