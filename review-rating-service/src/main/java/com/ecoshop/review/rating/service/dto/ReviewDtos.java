package com.ecoshop.review.rating.service.dto;

import com.ecoshop.review.rating.service.domain.ReviewStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ReviewDtos {

    public record SubmitReviewRequest(
            @NotNull UUID productId,
            UUID orderId,
            @Min(1) @Max(5) int rating,
            @Size(max = 200) String title,
            @Size(max = 5000) String body
    ) {}

    public record UpdateReviewRequest(
            @Min(1) @Max(5) int rating,
            @Size(max = 200) String title,
            @Size(max = 5000) String body
    ) {}

    public record ModerateRequest(
            @NotNull ReviewStatus status, // APPROVED | REJECTED | HIDDEN
            @Size(max = 500) String moderationNote
    ) {}

    public record ReviewResponse(
            UUID id,
            UUID productId,
            UUID userId,
            UUID orderId,
            int rating,
            String title,
            String body,
            boolean verifiedPurchase,
            ReviewStatus status,
            int helpfulCount,
            String moderationNote,
            Instant createdAt
    ) {}

    public record RatingSummaryResponse(
            UUID productId,
            int ratingCount,
            BigDecimal ratingAvg,
            int count1,
            int count2,
            int count3,
            int count4,
            int count5
    ) {}
}
