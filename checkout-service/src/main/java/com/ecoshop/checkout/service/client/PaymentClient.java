package com.ecoshop.checkout.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment-service", path = "/api/payments")
public interface PaymentClient {

    @PostMapping
    ApiResponse<PaymentResponse> createPayment(@RequestHeader("Authorization") String authHeader,
                                               @RequestBody CreatePaymentRequest req);

    record CreatePaymentRequest(UUID orderId, BigDecimal amount, String currency,
                                String method, String idempotencyKey) {}

    record PaymentResponse(
            UUID id, UUID orderId, UUID userId,
            BigDecimal amount, String currency, String method, String status,
            String gateway, String gatewayRef, String clientSecret, String checkoutUrl
    ) {}
}
