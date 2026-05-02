package com.ecoshop.returns.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "return_requests", indexes = {
        @Index(name = "idx_returns_order", columnList = "order_id"),
        @Index(name = "idx_returns_user", columnList = "user_id"),
        @Index(name = "idx_returns_status", columnList = "status"),
        @Index(name = "idx_returns_rma", columnList = "rma_number", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReturnRequest extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Customer-facing RMA number, e.g. RMA-2026-0000001 */
    @Column(name = "rma_number", nullable = false, unique = true, length = 50)
    private String rmaNumber;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReturnReason reason;

    @Column(length = 1000)
    private String reasonDetails;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_payment_id")
    private UUID refundPaymentId;

    @Column(name = "pickup_tracking_number", length = 100)
    private String pickupTrackingNumber;

    @Column(name = "pickup_scheduled_at")
    private Instant pickupScheduledAt;

    @Column(name = "qc_note", length = 1000)
    private String qcNote;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReturnItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReturnStatusHistory> statusHistory = new ArrayList<>();

    public void addItem(ReturnItem i) { items.add(i); i.setReturnRequest(this); }
    public void addStatusHistory(ReturnStatusHistory h) { statusHistory.add(h); h.setReturnRequest(this); }
}
