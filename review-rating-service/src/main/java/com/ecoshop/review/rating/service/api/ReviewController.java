package com.ecoshop.review.rating.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.review.rating.service.dto.ReviewDtos.*;
import com.ecoshop.review.rating.service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Public reads
    @GetMapping("/product/{productId}")
    public ApiResponse<PageResponse<ReviewResponse>> listByProduct(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return ApiResponse.ok(reviewService.listByProduct(productId, page, size, sort));
    }

    @GetMapping("/product/{productId}/summary")
    public ApiResponse<RatingSummaryResponse> getSummary(@PathVariable UUID productId) {
        return ApiResponse.ok(reviewService.getSummary(productId));
    }

    // Authenticated user actions
    @PostMapping
    public ApiResponse<ReviewResponse> submit(@AuthenticationPrincipal String userId,
                                              @Valid @RequestBody SubmitReviewRequest req) {
        return ApiResponse.ok(reviewService.submit(UUID.fromString(userId), req));
    }

    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> update(@AuthenticationPrincipal String userId,
                                              @PathVariable UUID reviewId,
                                              @Valid @RequestBody UpdateReviewRequest req) {
        return ApiResponse.ok(reviewService.update(UUID.fromString(userId), reviewId, req));
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal String userId,
                                    @PathVariable UUID reviewId) {
        reviewService.delete(UUID.fromString(userId), reviewId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{reviewId}/helpful")
    public ApiResponse<ReviewResponse> markHelpful(@PathVariable UUID reviewId) {
        return ApiResponse.ok(reviewService.markHelpful(reviewId));
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<ReviewResponse>> listMine(@AuthenticationPrincipal String userId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(reviewService.listMyReviews(UUID.fromString(userId), page, size));
    }

    // Moderation (ADMIN role)
    @GetMapping("/admin/pending")
    public ApiResponse<PageResponse<ReviewResponse>> listPending(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(reviewService.listPendingModeration(page, size));
    }

    @PostMapping("/admin/{reviewId}/moderate")
    public ApiResponse<ReviewResponse> moderate(@PathVariable UUID reviewId,
                                                @Valid @RequestBody ModerateRequest req) {
        return ApiResponse.ok(reviewService.moderate(reviewId, req));
    }
}
