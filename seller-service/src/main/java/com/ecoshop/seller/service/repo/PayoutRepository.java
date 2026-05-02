package com.ecoshop.seller.service.repo;

import com.ecoshop.seller.service.domain.Payout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, UUID> {
    Page<Payout> findBySellerIdOrderByPeriodEndDesc(UUID sellerId, Pageable pageable);
}
