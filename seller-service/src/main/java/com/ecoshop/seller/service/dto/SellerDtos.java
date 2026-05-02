package com.ecoshop.seller.service.dto;

import com.ecoshop.seller.service.domain.PayoutStatus;
import com.ecoshop.seller.service.domain.SellerStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class SellerDtos {

    public record OnboardSellerRequest(
            @NotBlank @Size(max = 200) String displayName,
            @NotBlank @Size(max = 300) String legalName,
            @NotBlank @Email String contactEmail,
            @Size(max = 20) String contactPhone,
            @Pattern(regexp = "[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9][A-Z][0-9A-Z]", message = "Invalid GSTIN")
            String gstin,
            @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN") String pan,
            @Size(max = 200) String bankAccountHolder,
            @Size(min = 4, max = 4) String bankAccountLast4,
            @Pattern(regexp = "[A-Z]{4}0[A-Z0-9]{6}", message = "Invalid IFSC") String bankIfsc
    ) {}

    public record SellerResponse(
            UUID id,
            UUID userId,
            String displayName,
            String legalName,
            String contactEmail,
            String contactPhone,
            String gstin,
            String pan,
            SellerStatus status,
            BigDecimal commissionRate,
            String bankAccountHolder,
            String bankAccountLast4,
            String bankIfsc,
            String rejectionReason,
            Instant createdAt
    ) {}

    public record VerifySellerRequest(
            @NotNull boolean approve,
            @Size(max = 500) String reason,
            @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal commissionRate
    ) {}

    public record SubmitProductRequest(
            @NotNull UUID productId
    ) {}

    public record ProductLinkResponse(
            UUID id,
            UUID sellerId,
            UUID productId,
            String approvalStatus,
            String approvalNote
    ) {}

    public record ApproveProductRequest(
            @NotNull boolean approve,
            @Size(max = 500) String note
    ) {}

    public record PayoutLineRequest(
            @NotNull UUID sellerId,
            @NotNull LocalDate periodStart,
            @NotNull LocalDate periodEnd,
            @NotNull @DecimalMin("0.0") BigDecimal grossSales,
            @DecimalMin("0.0") BigDecimal refunds
    ) {}

    public record PayoutResponse(
            UUID id,
            UUID sellerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal grossSales,
            BigDecimal commission,
            BigDecimal refunds,
            BigDecimal netPayout,
            String currency,
            PayoutStatus status,
            Instant settledAt,
            String externalRef
    ) {}
}
