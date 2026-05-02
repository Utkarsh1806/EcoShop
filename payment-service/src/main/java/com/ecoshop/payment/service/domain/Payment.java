package com.ecoshop.payment.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_order", columnList = "order_id"),
        @Index(name = "idx_payments_user", columnList = "user_id"),
        @Index(name = "idx_payments_gateway_ref", columnList = "gateway_ref"),
        @Index(name = "idx_payments_status", columnList = "status"),
        @Index(name = "idx_payments_idempotency", columnList = "idempotency_key", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(name = "gateway", length = 50)
    private String gateway; // razorpay | stripe | mock

    @Column(name = "gateway_ref", length = 100)
    private String gatewayRef; // gateway-side payment id

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "amount_refunded", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountRefunded = BigDecimal.ZERO;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;
}
