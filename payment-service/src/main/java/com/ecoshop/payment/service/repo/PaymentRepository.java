package com.ecoshop.payment.service.repo;

import com.ecoshop.payment.service.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByGatewayRef(String gatewayRef);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findByOrderId(UUID orderId);
}
