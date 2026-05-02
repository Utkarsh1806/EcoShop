package com.ecoshop.pricing.promotion.service.repo;

import com.ecoshop.pricing.promotion.service.domain.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, UUID> {
    long countByCouponIdAndUserId(UUID couponId, UUID userId);
    boolean existsByOrderId(UUID orderId);
}
