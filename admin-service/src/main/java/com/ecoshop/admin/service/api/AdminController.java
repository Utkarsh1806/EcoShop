package com.ecoshop.admin.service.api;

import com.ecoshop.admin.service.dto.AdminDtos.*;
import com.ecoshop.admin.service.service.AdminService;
import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin endpoints. Most operations route to other services through aggregator views;
 * everything is logged to the audit trail.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/audit")
    public ApiResponse<AuditLogResponse> recordAudit(@AuthenticationPrincipal String adminUserId,
                                                     @RequestHeader(value = "X-Admin-Email", required = false) String adminEmail,
                                                     @Valid @RequestBody AuditLogRequest req) {
        return ApiResponse.ok(adminService.recordAudit(UUID.fromString(adminUserId), adminEmail, req));
    }

    @GetMapping("/audit")
    public ApiResponse<PageResponse<AuditLogResponse>> listAudit(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.ok(adminService.listAuditLogs(page, size));
    }

    @GetMapping("/audit/target")
    public ApiResponse<PageResponse<AuditLogResponse>> listAuditForTarget(
            @RequestParam String targetType,
            @RequestParam String targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.ok(adminService.listAuditLogsForTarget(targetType, targetId, page, size));
    }

    @GetMapping("/orders/{orderId}/investigate")
    public ApiResponse<OrderInvestigationResponse> investigateOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID orderId) {
        return ApiResponse.ok(adminService.investigateOrder(orderId, authHeader));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserDetailResponse> getUserDetail(@PathVariable UUID userId) {
        return ApiResponse.ok(adminService.getUserDetail(userId));
    }

    @GetMapping("/sellers/{sellerId}")
    public ApiResponse<Object> getSellerDetail(@PathVariable UUID sellerId) {
        return ApiResponse.ok(adminService.getSellerDetail(sellerId));
    }
}
