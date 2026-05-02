package com.ecoshop.admin.service.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AdminDtos {

    public record UserDetailResponse(
            UUID userId,
            String email,
            String firstName,
            String lastName,
            String phone,
            String role,
            String status,
            boolean emailVerified,
            Instant createdAt
    ) {}

    public record OrderInvestigationResponse(
            UUID orderId,
            String orderNumber,
            UUID userId,
            String orderStatus,
            BigDecimal totalAmount,
            String currency,
            String paymentId,
            String paymentStatus,
            Object userDetails,
            Object fraudCheck,
            Instant orderCreatedAt
    ) {}

    public record AuditLogRequest(
            @NotBlank @Size(max = 100) String action,
            @Size(max = 50) String targetType,
            @Size(max = 200) String targetId,
            String ipAddress,
            String details,
            @NotBlank @Size(max = 20) String result
    ) {}

    public record AuditLogResponse(
            UUID id,
            UUID adminUserId,
            String adminEmail,
            String action,
            String targetType,
            String targetId,
            String ipAddress,
            String details,
            String result,
            Instant occurredAt
    ) {}
}
