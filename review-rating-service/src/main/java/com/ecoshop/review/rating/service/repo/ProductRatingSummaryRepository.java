package com.ecoshop.review.rating.service.repo;

import com.ecoshop.review.rating.service.domain.ProductRatingSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRatingSummaryRepository extends JpaRepository<ProductRatingSummary, UUID> {

    Optional<ProductRatingSummary> findByProductId(UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ProductRatingSummary s WHERE s.productId = :productId")
    Optional<ProductRatingSummary> findByProductIdForUpdate(@Param("productId") UUID productId);
}
