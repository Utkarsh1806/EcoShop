package com.ecoshop.payment.service.service;

import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.payment.service.domain.Payment;
import com.ecoshop.payment.service.domain.PaymentStatus;
import com.ecoshop.payment.service.dto.PaymentDtos.*;
import com.ecoshop.payment.service.gateway.PaymentGateway;
import com.ecoshop.payment.service.gateway.PaymentGateway.GatewayIntent;
import com.ecoshop.payment.service.gateway.PaymentGateway.GatewayResult;
import com.ecoshop.payment.service.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    public static final String TOPIC_PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String TOPIC_PAYMENT_FAILED = "payment.failed";

    private final PaymentRepository paymentRepository;
    private final PaymentGateway gateway;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public PaymentResponse createPayment(UUID userId, CreatePaymentRequest req) {
        // Idempotency check — return existing payment if same key was used before
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(req.idempotencyKey());
        if (existing.isPresent()) {
            log.info("Idempotent replay for key {}: returning existing payment {}",
                    req.idempotencyKey(), existing.get().getId());
            return toResponse(existing.get(), null, null);
        }

        GatewayIntent intent = gateway.createIntent(
                req.idempotencyKey(), req.amount(),
                req.currency() != null ? req.currency() : "INR",
                req.method().name());

        Payment payment = Payment.builder()
                .orderId(req.orderId())
                .userId(userId)
                .amount(req.amount())
                .currency(req.currency() != null ? req.currency() : "INR")
                .method(req.method())
                .status(PaymentStatus.CREATED)
                .gateway(gateway.name())
                .gatewayRef(intent.gatewayRef())
                .idempotencyKey(req.idempotencyKey())
                .build();
        payment = paymentRepository.save(payment);
        log.info("Payment created: id={} orderId={} ref={}", payment.getId(), req.orderId(), intent.gatewayRef());
        return toResponse(payment, intent.clientSecret(), intent.checkoutUrl());
    }

    /**
     * Confirms / captures a payment. Called either from the frontend after the user completes
     * the gateway-side flow, or from a webhook. Emits Kafka events on success/failure.
     */
    @Transactional
    public PaymentResponse capture(String gatewayRef) {
        Payment payment = paymentRepository.findByGatewayRef(gatewayRef)
                .orElseThrow(() -> BusinessException.notFound("PAYMENT_NOT_FOUND",
                        "No payment with gateway ref " + gatewayRef));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            log.info("Payment {} already succeeded — idempotent return", payment.getId());
            return toResponse(payment, null, null);
        }
        if (payment.getStatus() != PaymentStatus.CREATED && payment.getStatus() != PaymentStatus.PROCESSING) {
            throw BusinessException.badRequest("INVALID_STATE",
                    "Cannot capture payment in status " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.PROCESSING);
        GatewayResult result = gateway.capture(gatewayRef);

        if (result.success()) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);
            publishSucceeded(payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.message());
            paymentRepository.save(payment);
            publishFailed(payment, result.message());
        }
        return toResponse(payment, null, null);
    }

    @Transactional
    public RefundResponse refund(UUID paymentId, RefundRequest req) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessException.notFound("PAYMENT_NOT_FOUND",
                        "Payment " + paymentId + " not found"));
        if (payment.getStatus() != PaymentStatus.SUCCEEDED &&
            payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw BusinessException.badRequest("CANNOT_REFUND",
                    "Can only refund a SUCCEEDED or PARTIALLY_REFUNDED payment");
        }

        BigDecimal alreadyRefunded = payment.getAmountRefunded();
        BigDecimal newRefunded = alreadyRefunded.add(req.amount());
        if (newRefunded.compareTo(payment.getAmount()) > 0) {
            throw BusinessException.badRequest("REFUND_EXCEEDS_PAYMENT",
                    "Refund amount exceeds remaining capturable balance");
        }

        GatewayResult result = gateway.refund(payment.getGatewayRef(), req.amount());
        if (!result.success()) {
            throw BusinessException.badRequest("REFUND_FAILED", result.message());
        }
        payment.setAmountRefunded(newRefunded);
        payment.setStatus(newRefunded.compareTo(payment.getAmount()) == 0
                ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
        paymentRepository.save(payment);
        log.info("Payment {} refunded: total refunded {}", payment.getId(), newRefunded);

        return new RefundResponse(payment.getId(), newRefunded, payment.getStatus(), result.message());
    }

    @Transactional
    public PaymentResponse handleWebhook(String payload, String signature) {
        gateway.verifyWebhookSignature(payload, signature);
        // Naive parse — real Razorpay/Stripe payloads are richer.
        // For the mock gateway, payload format: { "gatewayRef": "...", "event": "captured" | "failed" }
        try {
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
            String ref = node.get("gatewayRef").asText();
            String event = node.get("event").asText();
            if ("captured".equalsIgnoreCase(event)) {
                return capture(ref);
            } else if ("failed".equalsIgnoreCase(event)) {
                Payment p = paymentRepository.findByGatewayRef(ref)
                        .orElseThrow(() -> BusinessException.notFound("PAYMENT_NOT_FOUND",
                                "No payment for ref " + ref));
                p.setStatus(PaymentStatus.FAILED);
                p.setFailureReason(node.has("reason") ? node.get("reason").asText() : "Webhook reported failure");
                paymentRepository.save(p);
                publishFailed(p, p.getFailureReason());
                return toResponse(p, null, null);
            }
            throw BusinessException.badRequest("UNSUPPORTED_EVENT", "Unknown event " + event);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw BusinessException.badRequest("INVALID_PAYLOAD", "Failed to parse webhook: " + e.getMessage());
        }
    }

    private void publishSucceeded(Payment p) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventId", UUID.randomUUID().toString());
        evt.put("paymentId", p.getId().toString());
        evt.put("orderId", p.getOrderId().toString());
        evt.put("gatewayRef", p.getGatewayRef());
        evt.put("amount", p.getAmount().toPlainString());
        evt.put("currency", p.getCurrency());
        kafkaTemplate.send(TOPIC_PAYMENT_SUCCEEDED, p.getOrderId().toString(), evt);
    }

    private void publishFailed(Payment p, String reason) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventId", UUID.randomUUID().toString());
        evt.put("paymentId", p.getId().toString());
        evt.put("orderId", p.getOrderId().toString());
        evt.put("reason", reason);
        kafkaTemplate.send(TOPIC_PAYMENT_FAILED, p.getOrderId().toString(), evt);
    }

    public PaymentResponse toResponse(Payment p, String clientSecret, String checkoutUrl) {
        return new PaymentResponse(
                p.getId(), p.getOrderId(), p.getUserId(),
                p.getAmount(), p.getCurrency(), p.getMethod(), p.getStatus(),
                p.getGateway(), p.getGatewayRef(), clientSecret, checkoutUrl
        );
    }
}
