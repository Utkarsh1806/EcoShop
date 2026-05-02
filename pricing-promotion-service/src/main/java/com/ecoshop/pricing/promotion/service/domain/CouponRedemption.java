package com.ecoshop.pricing.promotion.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "coupon_redemptions", indexes = {
        @Index(name = "idx_redemptions_coupon", columnList = "coupon_id"),
        @Index(name = "idx_redemptions_user", columnList = "user_id"),
        @Index(name = "idx_redemptions_order", columnList = "order_id", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CouponRedemption extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "coupon_id", nullable = false)
    private UUID couponId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "discount_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountApplied;
}
