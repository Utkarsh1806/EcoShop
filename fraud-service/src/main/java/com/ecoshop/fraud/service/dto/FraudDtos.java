package com.ecoshop.fraud.service.dto;

import com.ecoshop.fraud.service.domain.RiskLevel;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FraudDtos {

    public record EvaluateOrderRequest(
            @NotNull UUID orderId,
            @NotNull UUID userId,
            String userEmail,
            String userPhone,
            String ipAddress,
            String deviceId,
            @NotNull @DecimalMin("0.0") BigDecimal totalAmount,
            String currency,
            @Min(1) int itemCount,
            String shippingPostalCode,
            String billingPostalCode,
            Boolean isFirstOrder
    ) {}

    public record RuleHitDto(
            String ruleCode,
            String ruleDescription,
            int scoreAdded,
            String evidence
    ) {}

    public record FraudDecisionResponse(
            UUID id,
            String subjectType,
            UUID subjectId,
            UUID userId,
            int riskScore,
            RiskLevel riskLevel,
            String decision,
            boolean manualReviewRequired,
            List<RuleHitDto> hits,
            Instant evaluatedAt
    ) {}

    public record BlocklistEntryRequest(
            @NotBlank String entryType,
            @NotBlank @Size(max = 200) String entryValue,
            @Size(max = 500) String reason,
            Instant expiresAt
    ) {}

    public record BlocklistEntryResponse(
            UUID id,
            String entryType,
            String entryValue,
            String reason,
            String addedBy,
            Instant expiresAt
    ) {}
}
