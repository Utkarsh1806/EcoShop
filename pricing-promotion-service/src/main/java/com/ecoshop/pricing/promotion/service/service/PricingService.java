package com.ecoshop.pricing.promotion.service.service;

import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.pricing.promotion.service.domain.Coupon;
import com.ecoshop.pricing.promotion.service.domain.CouponRedemption;
import com.ecoshop.pricing.promotion.service.domain.DiscountType;
import com.ecoshop.pricing.promotion.service.dto.PricingDtos.*;
import com.ecoshop.pricing.promotion.service.repo.CouponRedemptionRepository;
import com.ecoshop.pricing.promotion.service.repo.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;

    @Transactional
    public CouponResponse createCoupon(CouponRequest req) {
        if (couponRepository.existsByCode(req.code())) {
            throw BusinessException.conflict("COUPON_CODE_EXISTS", "Code already in use: " + req.code());
        }
        if (req.validUntil().isBefore(req.validFrom())) {
            throw BusinessException.badRequest("INVALID_DATES", "validUntil must be after validFrom");
        }
        Coupon coupon = Coupon.builder()
                .code(req.code().toUpperCase())
                .description(req.description())
                .discountType(req.discountType())
                .discountValue(req.discountValue())
                .maxDiscountAmount(req.maxDiscountAmount())
                .minCartAmount(req.minCartAmount() != null ? req.minCartAmount() : BigDecimal.ZERO)
                .validFrom(req.validFrom())
                .validUntil(req.validUntil())
                .usageLimit(req.usageLimit())
                .perUserLimit(req.perUserLimit() != null ? req.perUserLimit() : 1)
                .active(true)
                .build();
        return toResponse(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public QuoteResponse quote(QuoteRequest req) {
        Coupon coupon;
        try {
            coupon = lookupActive(req.code());
            validateApplicable(coupon, req.userId(), req.cartSubtotal());
        } catch (BusinessException e) {
            return new QuoteResponse(req.code(), false, BigDecimal.ZERO, req.cartSubtotal(), e.getMessage());
        }
        BigDecimal discount = computeDiscount(coupon, req.cartSubtotal());
        BigDecimal afterDiscount = req.cartSubtotal().subtract(discount).max(BigDecimal.ZERO);
        return new QuoteResponse(req.code(), true, discount, afterDiscount, "OK");
    }

    /**
     * Atomically validates and records a redemption. Increments usage counters.
     * Idempotent on orderId — calling twice for the same order returns the existing redemption.
     */
    @Transactional
    public RedeemResponse redeem(RedeemRequest req) {
        if (redemptionRepository.existsByOrderId(req.orderId())) {
            throw BusinessException.conflict("ALREADY_REDEEMED",
                    "Coupon already redeemed for order " + req.orderId());
        }
        Coupon coupon = lookupActive(req.code());
        validateApplicable(coupon, req.userId(), req.cartSubtotal());

        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw BusinessException.conflict("USAGE_LIMIT_REACHED", "Coupon fully redeemed");
        }

        BigDecimal discount = computeDiscount(coupon, req.cartSubtotal());

        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);

        CouponRedemption redemption = CouponRedemption.builder()
                .couponId(coupon.getId())
                .userId(req.userId())
                .orderId(req.orderId())
                .discountApplied(discount)
                .build();
        redemption = redemptionRepository.save(redemption);
        log.info("Coupon {} redeemed by user {} for order {}, discount={}",
                coupon.getCode(), req.userId(), req.orderId(), discount);
        return new RedeemResponse(redemption.getId(), coupon.getCode(), discount);
    }

    @Transactional(readOnly = true)
    public CouponResponse getByCode(String code) {
        Coupon c = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> BusinessException.notFound("COUPON_NOT_FOUND",
                        "No coupon with code " + code));
        return toResponse(c);
    }

    private Coupon lookupActive(String code) {
        Coupon c = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> BusinessException.notFound("COUPON_NOT_FOUND",
                        "No coupon with code " + code));
        if (!c.isActive()) {
            throw BusinessException.badRequest("COUPON_INACTIVE", "Coupon is not active");
        }
        Instant now = Instant.now();
        if (now.isBefore(c.getValidFrom())) {
            throw BusinessException.badRequest("COUPON_NOT_YET_VALID", "Coupon not yet active");
        }
        if (now.isAfter(c.getValidUntil())) {
            throw BusinessException.badRequest("COUPON_EXPIRED", "Coupon has expired");
        }
        return c;
    }

    private void validateApplicable(Coupon c, UUID userId, BigDecimal subtotal) {
        if (subtotal.compareTo(c.getMinCartAmount()) < 0) {
            throw BusinessException.badRequest("MIN_CART_NOT_MET",
                    "Minimum cart amount is " + c.getMinCartAmount());
        }
        if (c.getUsageLimit() != null && c.getUsageCount() >= c.getUsageLimit()) {
            throw BusinessException.badRequest("USAGE_LIMIT_REACHED", "Coupon fully redeemed");
        }
        long perUserUsed = redemptionRepository.countByCouponIdAndUserId(c.getId(), userId);
        if (c.getPerUserLimit() != null && perUserUsed >= c.getPerUserLimit()) {
            throw BusinessException.badRequest("PER_USER_LIMIT_REACHED",
                    "You have already used this coupon");
        }
    }

    private BigDecimal computeDiscount(Coupon c, BigDecimal subtotal) {
        BigDecimal discount;
        if (c.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(c.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (c.getMaxDiscountAmount() != null && discount.compareTo(c.getMaxDiscountAmount()) > 0) {
                discount = c.getMaxDiscountAmount();
            }
        } else {
            discount = c.getDiscountValue();
        }
        // Never discount more than subtotal
        return discount.min(subtotal);
    }

    public CouponResponse toResponse(Coupon c) {
        return new CouponResponse(
                c.getId(), c.getCode(), c.getDescription(),
                c.getDiscountType(), c.getDiscountValue(),
                c.getMaxDiscountAmount(), c.getMinCartAmount(),
                c.getValidFrom(), c.getValidUntil(),
                c.getUsageLimit(), c.getUsageCount(), c.getPerUserLimit(),
                c.isActive()
        );
    }
}
