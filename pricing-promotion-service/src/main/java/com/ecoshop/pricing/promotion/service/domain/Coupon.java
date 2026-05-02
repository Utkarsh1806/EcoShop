package com.ecoshop.pricing.promotion.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupons_code", columnList = "code", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    /** For PERCENTAGE: 10 means 10%. For FLAT_AMOUNT: amount in currency units. */
    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    /** Cap on discount when type=PERCENTAGE (e.g. "10% off, max ₹500"). Null = no cap. */
    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    /** Minimum cart subtotal to qualify */
    @Column(name = "min_cart_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minCartAmount = BigDecimal.ZERO;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "usage_limit")
    private Integer usageLimit; // null = unlimited

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "per_user_limit")
    @Builder.Default
    private Integer perUserLimit = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
