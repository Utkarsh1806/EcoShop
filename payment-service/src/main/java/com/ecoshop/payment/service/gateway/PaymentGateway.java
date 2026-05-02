package com.ecoshop.payment.service.gateway;

import java.math.BigDecimal;

/**
 * Port for external payment gateways. Concrete implementations:
 * <ul>
 *   <li>{@link MockPaymentGateway} — for local dev / demos</li>
 *   <li>RazorpayGateway / StripeGateway — to be added with vendor SDKs</li>
 * </ul>
 */
public interface PaymentGateway {

    String name();

    /** Create a payment intent at the gateway and return its reference id. */
    GatewayIntent createIntent(String idempotencyKey, BigDecimal amount, String currency, String method);

    /** Capture a previously authorized payment. Returns SUCCEEDED or FAILED. */
    GatewayResult capture(String gatewayRef);

    /** Refund (full or partial). */
    GatewayResult refund(String gatewayRef, BigDecimal amount);

    /** Verify a webhook signature. Throws if invalid. */
    void verifyWebhookSignature(String payload, String signature);

    record GatewayIntent(
            String gatewayRef,
            String clientSecret, // returned to the frontend, used to complete payment
            String checkoutUrl   // optional, gateway-hosted page
    ) {}

    record GatewayResult(
            boolean success,
            String gatewayRef,
            String message
    ) {}
}
