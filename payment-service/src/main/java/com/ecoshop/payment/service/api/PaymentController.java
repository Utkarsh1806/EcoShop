package com.ecoshop.payment.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.payment.service.dto.PaymentDtos.*;
import com.ecoshop.payment.service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> create(@AuthenticationPrincipal String userId,
                                                @Valid @RequestBody CreatePaymentRequest req) {
        return ApiResponse.ok(paymentService.createPayment(UUID.fromString(userId), req));
    }

    @PostMapping("/capture")
    public ApiResponse<PaymentResponse> capture(@Valid @RequestBody CapturePaymentRequest req) {
        return ApiResponse.ok(paymentService.capture(req.gatewayRef()));
    }

    @PostMapping("/{paymentId}/refund")
    public ApiResponse<RefundResponse> refund(@PathVariable UUID paymentId,
                                              @Valid @RequestBody RefundRequest req) {
        return ApiResponse.ok(paymentService.refund(paymentId, req));
    }

    /**
     * Webhook endpoint. Public (signature-protected). Configure your gateway dashboard to POST here.
     * Body must be the raw JSON the gateway sent; signature is in the X-Webhook-Signature header.
     */
    @PostMapping("/webhook")
    public ApiResponse<PaymentResponse> webhook(@RequestBody String payload,
                                                @RequestHeader("X-Webhook-Signature") String signature) {
        return ApiResponse.ok(paymentService.handleWebhook(payload, signature));
    }
}
