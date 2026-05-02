package com.ecoshop.order.service.repo;

import com.ecoshop.order.service.domain.Order;
import com.ecoshop.order.service.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByUserId(UUID userId, Pageable pageable);
    Page<Order> findByUserIdAndStatus(UUID userId, OrderStatus status, Pageable pageable);
    Optional<Order> findByPaymentId(String paymentId);
}
