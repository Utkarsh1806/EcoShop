package com.ecoshop.checkout.service.repo;

import com.ecoshop.checkout.service.domain.CheckoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, UUID> {
    Optional<CheckoutSession> findByIdempotencyKey(String idempotencyKey);
}
