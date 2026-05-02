package com.ecoshop.returns.service.repo;

import com.ecoshop.returns.service.domain.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, UUID> {
    Optional<ReturnRequest> findByRmaNumber(String rmaNumber);
    Page<ReturnRequest> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<ReturnRequest> findByOrderId(UUID orderId, Pageable pageable);
}
