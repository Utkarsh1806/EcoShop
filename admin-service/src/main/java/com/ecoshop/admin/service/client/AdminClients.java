package com.ecoshop.admin.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AdminClients {

    @FeignClient(name = "user-service", path = "/api/users")
    public interface UserClient {
        @GetMapping("/internal/{userId}")
        ApiResponse<UserView> getById(@PathVariable("userId") UUID userId);

        record UserView(UUID id, String email, String firstName, String lastName,
                        String phone, String role, String status,
                        boolean emailVerified, Instant createdAt) {}
    }

    @FeignClient(name = "order-service", path = "/api/orders")
    public interface OrderClient {
        @GetMapping("/{orderId}")
        ApiResponse<OrderSummary> getById(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable("orderId") UUID orderId);

        record OrderSummary(UUID id, String orderNumber, UUID userId, String status,
                            BigDecimal totalAmount, String currency,
                            String paymentId, String paymentStatus, Instant createdAt) {}
    }

    @FeignClient(name = "fraud-service", path = "/api/fraud")
    public interface FraudAdminClient {
        @GetMapping("/checks/order/{orderId}/latest")
        ApiResponse<FraudView> getLatestForOrder(@PathVariable("orderId") UUID orderId);

        record FraudView(UUID id, String subjectType, UUID subjectId, UUID userId,
                         int riskScore, String riskLevel, String decision,
                         boolean manualReviewRequired, List<Object> hits, Instant evaluatedAt) {}
    }

    @FeignClient(name = "seller-service", path = "/api/sellers")
    public interface SellerAdminClient {
        @GetMapping("/{sellerId}")
        ApiResponse<SellerView> getById(@PathVariable("sellerId") UUID sellerId);

        record SellerView(UUID id, UUID userId, String displayName, String legalName,
                          String contactEmail, String contactPhone, String gstin, String pan,
                          String status, BigDecimal commissionRate,
                          String bankAccountHolder, String bankAccountLast4, String bankIfsc,
                          String rejectionReason, Instant createdAt) {}
    }
}
