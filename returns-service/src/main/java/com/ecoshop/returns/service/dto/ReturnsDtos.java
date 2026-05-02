package com.ecoshop.returns.service.dto;

import com.ecoshop.returns.service.domain.ReturnReason;
import com.ecoshop.returns.service.domain.ReturnStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReturnsDtos {

    public record ReturnItemRequest(
            @NotNull UUID orderItemId,
            @Min(1) int quantity
    ) {}

    public record CreateReturnRequest(
            @NotNull UUID orderId,
            @NotNull ReturnReason reason,
            @Size(max = 1000) String reasonDetails,
            @NotEmpty @Valid List<ReturnItemRequest> items
    ) {}

    public record ApproveRequest(
            Instant pickupScheduledAt
    ) {}

    public record RejectRequest(
            @NotBlank @Size(max = 500) String rejectionReason
    ) {}

    public record QcResultRequest(
            @NotNull boolean passed,
            @Size(max = 1000) String note
    ) {}

    public record ReturnItemResponse(
            UUID id,
            UUID orderItemId,
            UUID productId,
            String sku,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineRefundAmount
    ) {}

    public record ReturnStatusHistoryResponse(
            ReturnStatus fromStatus,
            ReturnStatus toStatus,
            String note,
            String changedBy,
            Instant occurredAt
    ) {}

    public record ReturnResponse(
            UUID id,
            String rmaNumber,
            UUID orderId,
            UUID userId,
            ReturnStatus status,
            ReturnReason reason,
            String reasonDetails,
            BigDecimal refundAmount,
            UUID refundPaymentId,
            String pickupTrackingNumber,
            Instant pickupScheduledAt,
            String qcNote,
            String rejectionReason,
            List<ReturnItemResponse> items,
            List<ReturnStatusHistoryResponse> statusHistory,
            Instant createdAt
    ) {}
}
