package com.ecoshop.seller.service.repo;

import com.ecoshop.seller.service.domain.Seller;
import com.ecoshop.seller.service.domain.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Optional<Seller> findByUserId(UUID userId);
    Optional<Seller> findByGstin(String gstin);
    Page<Seller> findByStatus(SellerStatus status, Pageable pageable);
    boolean existsByGstin(String gstin);
}
