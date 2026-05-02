package com.ecoshop.review.rating.service.repo;

import com.ecoshop.review.rating.service.domain.Review;
import com.ecoshop.review.rating.service.domain.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByUserIdAndProductId(UUID userId, UUID productId);
    Page<Review> findByProductIdAndStatus(UUID productId, ReviewStatus status, Pageable pageable);
    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<Review> findByStatusOrderByCreatedAtAsc(ReviewStatus status, Pageable pageable);
}
