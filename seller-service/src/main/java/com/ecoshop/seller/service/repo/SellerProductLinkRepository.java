package com.ecoshop.seller.service.repo;

import com.ecoshop.seller.service.domain.SellerProductLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerProductLinkRepository extends JpaRepository<SellerProductLink, UUID> {
    Optional<SellerProductLink> findByProductId(UUID productId);
    Page<SellerProductLink> findBySellerId(UUID sellerId, Pageable pageable);
    Page<SellerProductLink> findByApprovalStatus(String approvalStatus, Pageable pageable);
}
