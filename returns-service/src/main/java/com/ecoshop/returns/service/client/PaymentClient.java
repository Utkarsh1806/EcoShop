package com.ecoshop.returns.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment-service", path = "/api/payments")
public interface PaymentClient {

    @PostMapping("/{paymentId}/refund")
    ApiResponse<RefundResponse> refund(@PathVariable("paymentId") UUID paymentId,
                                       @RequestBody RefundRequest req);

    record RefundRequest(BigDecimal amount, String reason) {}

    record RefundResponse(
            UUID paymentId,
            BigDecimal amountRefunded,
            String status,
            String message
    ) {}
}
