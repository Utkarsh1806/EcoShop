package com.ecoshop.fraud.service.repo;

import com.ecoshop.fraud.service.domain.FraudCheck;
import com.ecoshop.fraud.service.domain.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudCheckRepository extends JpaRepository<FraudCheck, UUID> {
    Optional<FraudCheck> findFirstBySubjectTypeAndSubjectIdOrderByCreatedAtDesc(
            String subjectType, UUID subjectId);
    Page<FraudCheck> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);
    long countByUserIdAndCreatedAtAfter(UUID userId, Instant cutoff);
    List<FraudCheck> findByUserIdAndCreatedAtAfter(UUID userId, Instant cutoff);
}
