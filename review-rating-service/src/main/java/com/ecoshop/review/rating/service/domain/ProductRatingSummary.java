package com.ecoshop.review.rating.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Denormalized rating summary per product. Maintained on every approved review.
 * Cheaper to read than scanning all reviews; product-catalog-service can subscribe to
 * rating.updated events to update its own rating fields.
 */
@Entity
@Table(name = "product_rating_summaries", indexes = {
        @Index(name = "idx_rating_summary_product", columnList = "product_id", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductRatingSummary extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "rating_count", nullable = false)
    @Builder.Default
    private int ratingCount = 0;

    @Column(name = "rating_sum", nullable = false)
    @Builder.Default
    private long ratingSum = 0; // sum of all ratings, used to derive avg without floating point drift

    @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "count_1", nullable = false) @Builder.Default private int count1 = 0;
    @Column(name = "count_2", nullable = false) @Builder.Default private int count2 = 0;
    @Column(name = "count_3", nullable = false) @Builder.Default private int count3 = 0;
    @Column(name = "count_4", nullable = false) @Builder.Default private int count4 = 0;
    @Column(name = "count_5", nullable = false) @Builder.Default private int count5 = 0;
}
