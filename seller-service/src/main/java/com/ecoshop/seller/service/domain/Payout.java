package com.ecoshop.seller.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payouts", indexes = {
        @Index(name = "idx_payouts_seller", columnList = "seller_id"),
        @Index(name = "idx_payouts_status", columnList = "status"),
        @Index(name = "idx_payouts_period", columnList = "period_start,period_end")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payout extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "gross_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossSales;

    @Column(name = "commission", nullable = false, precision = 14, scale = 2)
    private BigDecimal commission;

    @Column(name = "refunds", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal refunds = BigDecimal.ZERO;

    @Column(name = "net_payout", nullable = false, precision = 14, scale = 2)
    private BigDecimal netPayout;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "external_ref", length = 100)
    private String externalRef;
}
