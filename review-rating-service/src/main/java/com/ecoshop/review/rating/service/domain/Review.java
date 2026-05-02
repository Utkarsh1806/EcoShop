package com.ecoshop.review.rating.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_product", columnList = "product_id"),
        @Index(name = "idx_reviews_user", columnList = "user_id"),
        @Index(name = "idx_reviews_status", columnList = "status"),
        @Index(name = "idx_reviews_user_product", columnList = "user_id,product_id", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Review extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** Order from which this review originated, for "verified purchase" badge */
    @Column(name = "order_id")
    private UUID orderId;

    @Column(nullable = false)
    private int rating; // 1-5

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "verified_purchase", nullable = false)
    @Builder.Default
    private boolean verifiedPurchase = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING_MODERATION;

    @Column(name = "helpful_count", nullable = false)
    @Builder.Default
    private int helpfulCount = 0;

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;
}
