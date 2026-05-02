package com.ecoshop.review.rating.service.service;

import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.review.rating.service.domain.ProductRatingSummary;
import com.ecoshop.review.rating.service.domain.Review;
import com.ecoshop.review.rating.service.domain.ReviewStatus;
import com.ecoshop.review.rating.service.dto.ReviewDtos.*;
import com.ecoshop.review.rating.service.repo.ProductRatingSummaryRepository;
import com.ecoshop.review.rating.service.repo.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    public static final String TOPIC_REVIEW_SUBMITTED = "review.submitted";
    public static final String TOPIC_PRODUCT_RATING_UPDATED = "product.rating.updated";

    private final ReviewRepository reviewRepository;
    private final ProductRatingSummaryRepository summaryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${ecoshop.reviews.auto-approve:false}")
    private boolean autoApprove;

    @Transactional
    public ReviewResponse submit(UUID userId, SubmitReviewRequest req) {
        // Per-product per-user uniqueness
        if (reviewRepository.findByUserIdAndProductId(userId, req.productId()).isPresent()) {
            throw BusinessException.conflict("REVIEW_ALREADY_EXISTS",
                    "You've already reviewed this product. Edit your existing review instead.");
        }

        boolean verified = req.orderId() != null;
        // In production: verify the orderId belongs to userId AND contains productId via order-service Feign call.
        // Skipped here to keep service decoupled — can be added with a Feign client to order-service.

        ReviewStatus initialStatus = autoApprove ? ReviewStatus.APPROVED : ReviewStatus.PENDING_MODERATION;

        Review review = Review.builder()
                .userId(userId)
                .productId(req.productId())
                .orderId(req.orderId())
                .rating(req.rating())
                .title(req.title())
                .body(req.body())
                .verifiedPurchase(verified)
                .status(initialStatus)
                .build();
        review = reviewRepository.save(review);

        // Publish for fraud detection / spam check
        publishSubmitted(review);

        // If auto-approving, immediately apply to summary
        if (initialStatus == ReviewStatus.APPROVED) {
            applyToSummary(review.getProductId(), review.getRating(), 0);
        }

        log.info("Review {} submitted: productId={} userId={} rating={} status={}",
                review.getId(), req.productId(), userId, req.rating(), initialStatus);
        return toResponse(review);
    }

    @Transactional
    public ReviewResponse update(UUID userId, UUID reviewId, UpdateReviewRequest req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.notFound("REVIEW_NOT_FOUND",
                        "Review " + reviewId + " not found"));
        if (!review.getUserId().equals(userId)) {
            throw BusinessException.badRequest("FORBIDDEN", "Not your review");
        }

        // If approved, edits force re-moderation
        int oldRating = review.getRating();
        boolean wasApproved = review.getStatus() == ReviewStatus.APPROVED;

        review.setRating(req.rating());
        review.setTitle(req.title());
        review.setBody(req.body());
        if (wasApproved && !autoApprove) {
            review.setStatus(ReviewStatus.PENDING_MODERATION);
            // Roll back the old rating from the summary since it's no longer approved
            applyToSummary(review.getProductId(), 0, oldRating);
        } else if (wasApproved) {
            // Auto-approve: replace old rating with new
            applyToSummary(review.getProductId(), req.rating(), oldRating);
        }
        return toResponse(review);
    }

    @Transactional
    public void delete(UUID userId, UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.notFound("REVIEW_NOT_FOUND",
                        "Review " + reviewId + " not found"));
        if (!review.getUserId().equals(userId)) {
            throw BusinessException.badRequest("FORBIDDEN", "Not your review");
        }
        if (review.getStatus() == ReviewStatus.APPROVED) {
            applyToSummary(review.getProductId(), 0, review.getRating());
        }
        reviewRepository.delete(review);
        log.info("Review {} deleted by user {}", reviewId, userId);
    }

    @Transactional
    public ReviewResponse moderate(UUID reviewId, ModerateRequest req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.notFound("REVIEW_NOT_FOUND",
                        "Review " + reviewId + " not found"));
        ReviewStatus oldStatus = review.getStatus();
        review.setStatus(req.status());
        review.setModerationNote(req.moderationNote());

        // Apply summary changes based on transition
        if (oldStatus != ReviewStatus.APPROVED && req.status() == ReviewStatus.APPROVED) {
            applyToSummary(review.getProductId(), review.getRating(), 0);
        } else if (oldStatus == ReviewStatus.APPROVED && req.status() != ReviewStatus.APPROVED) {
            applyToSummary(review.getProductId(), 0, review.getRating());
        }
        log.info("Review {} moderated: {} -> {}", reviewId, oldStatus, req.status());
        return toResponse(review);
    }

    @Transactional
    public ReviewResponse markHelpful(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> BusinessException.notFound("REVIEW_NOT_FOUND",
                        "Review " + reviewId + " not found"));
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listByProduct(UUID productId, int page, int size, String sortBy) {
        Sort sort = switch (sortBy != null ? sortBy : "newest") {
            case "highest_rating" -> Sort.by(Sort.Direction.DESC, "rating");
            case "lowest_rating" -> Sort.by(Sort.Direction.ASC, "rating");
            case "most_helpful" -> Sort.by(Sort.Direction.DESC, "helpfulCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Page<Review> result = reviewRepository.findByProductIdAndStatus(
                productId, ReviewStatus.APPROVED, PageRequest.of(page, size, sort));
        List<ReviewResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listMyReviews(UUID userId, int page, int size) {
        Page<Review> result = reviewRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size));
        List<ReviewResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listPendingModeration(int page, int size) {
        Page<Review> result = reviewRepository.findByStatusOrderByCreatedAtAsc(
                ReviewStatus.PENDING_MODERATION, PageRequest.of(page, size));
        List<ReviewResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public RatingSummaryResponse getSummary(UUID productId) {
        ProductRatingSummary s = summaryRepository.findByProductId(productId)
                .orElseGet(() -> ProductRatingSummary.builder()
                        .productId(productId)
                        .build());
        return new RatingSummaryResponse(
                productId, s.getRatingCount(), s.getRatingAvg(),
                s.getCount1(), s.getCount2(), s.getCount3(), s.getCount4(), s.getCount5()
        );
    }

    /**
     * Apply rating delta to product summary. addRating: rating to add (0 = no add).
     * removeRating: rating to remove (0 = no remove). Atomic via pessimistic lock.
     */
    private void applyToSummary(UUID productId, int addRating, int removeRating) {
        ProductRatingSummary summary = summaryRepository.findByProductIdForUpdate(productId)
                .orElseGet(() -> summaryRepository.save(ProductRatingSummary.builder()
                        .productId(productId).build()));

        if (removeRating > 0) {
            summary.setRatingCount(Math.max(0, summary.getRatingCount() - 1));
            summary.setRatingSum(Math.max(0, summary.getRatingSum() - removeRating));
            decrementBucket(summary, removeRating);
        }
        if (addRating > 0) {
            summary.setRatingCount(summary.getRatingCount() + 1);
            summary.setRatingSum(summary.getRatingSum() + addRating);
            incrementBucket(summary, addRating);
        }
        summary.setRatingAvg(summary.getRatingCount() == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(summary.getRatingSum())
                        .divide(BigDecimal.valueOf(summary.getRatingCount()), 2, RoundingMode.HALF_UP));
        summaryRepository.save(summary);

        // Notify interested services (e.g. catalog can update its denormalized rating)
        publishRatingUpdated(productId, summary);
    }

    private void incrementBucket(ProductRatingSummary s, int rating) {
        switch (rating) {
            case 1 -> s.setCount1(s.getCount1() + 1);
            case 2 -> s.setCount2(s.getCount2() + 1);
            case 3 -> s.setCount3(s.getCount3() + 1);
            case 4 -> s.setCount4(s.getCount4() + 1);
            case 5 -> s.setCount5(s.getCount5() + 1);
            default -> { /* invalid rating */ }
        }
    }

    private void decrementBucket(ProductRatingSummary s, int rating) {
        switch (rating) {
            case 1 -> s.setCount1(Math.max(0, s.getCount1() - 1));
            case 2 -> s.setCount2(Math.max(0, s.getCount2() - 1));
            case 3 -> s.setCount3(Math.max(0, s.getCount3() - 1));
            case 4 -> s.setCount4(Math.max(0, s.getCount4() - 1));
            case 5 -> s.setCount5(Math.max(0, s.getCount5() - 1));
            default -> { /* invalid */ }
        }
    }

    private void publishSubmitted(Review review) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventId", UUID.randomUUID().toString());
        evt.put("occurredAt", Instant.now().toString());
        evt.put("reviewId", review.getId().toString());
        evt.put("productId", review.getProductId().toString());
        evt.put("userId", review.getUserId().toString());
        evt.put("rating", review.getRating());
        evt.put("verifiedPurchase", review.isVerifiedPurchase());
        kafkaTemplate.send(TOPIC_REVIEW_SUBMITTED, review.getProductId().toString(), evt);
    }

    private void publishRatingUpdated(UUID productId, ProductRatingSummary summary) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventId", UUID.randomUUID().toString());
        evt.put("occurredAt", Instant.now().toString());
        evt.put("productId", productId.toString());
        evt.put("ratingCount", summary.getRatingCount());
        evt.put("ratingAvg", summary.getRatingAvg().toPlainString());
        kafkaTemplate.send(TOPIC_PRODUCT_RATING_UPDATED, productId.toString(), evt);
    }

    public ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(), r.getProductId(), r.getUserId(), r.getOrderId(),
                r.getRating(), r.getTitle(), r.getBody(),
                r.isVerifiedPurchase(), r.getStatus(), r.getHelpfulCount(),
                r.getModerationNote(), r.getCreatedAt()
        );
    }
}
