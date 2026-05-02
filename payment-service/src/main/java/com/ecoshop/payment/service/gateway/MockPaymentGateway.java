package com.ecoshop.payment.service.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Mock gateway: simulates a payment flow end-to-end without contacting any external service.
 * - Always returns success on capture (configurable to simulate failures).
 * - Webhook signature verification uses HMAC-SHA256 with a shared secret.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    private final String webhookSecret;
    private final boolean alwaysSucceed;

    public MockPaymentGateway(
            @Value("${ecoshop.payment.mock.webhook-secret:dev-webhook-secret}") String webhookSecret,
            @Value("${ecoshop.payment.mock.always-succeed:true}") boolean alwaysSucceed) {
        this.webhookSecret = webhookSecret;
        this.alwaysSucceed = alwaysSucceed;
    }

    @Override
    public String name() { return "mock"; }

    @Override
    public GatewayIntent createIntent(String idempotencyKey, BigDecimal amount, String currency, String method) {
        String ref = "mock_pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String secret = "mock_cs_" + UUID.randomUUID().toString().replace("-", "");
        return new GatewayIntent(ref, secret, "https://mock-checkout.local/" + ref);
    }

    @Override
    public GatewayResult capture(String gatewayRef) {
        if (alwaysSucceed) {
            return new GatewayResult(true, gatewayRef, "Captured (mock)");
        }
        return new GatewayResult(false, gatewayRef, "Mock failure");
    }

    @Override
    public GatewayResult refund(String gatewayRef, BigDecimal amount) {
        return new GatewayResult(true, "mock_rf_" + UUID.randomUUID(), "Refunded (mock) " + amount);
    }

    @Override
    public void verifyWebhookSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(hash);
            if (!expected.equalsIgnoreCase(signature)) {
                throw new SecurityException("Invalid webhook signature");
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Signature verification failed: " + e.getMessage(), e);
        }
    }
}
