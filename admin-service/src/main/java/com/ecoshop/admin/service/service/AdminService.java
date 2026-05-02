package com.ecoshop.admin.service.service;

import com.ecoshop.admin.service.client.AdminClients.FraudAdminClient;
import com.ecoshop.admin.service.client.AdminClients.OrderClient;
import com.ecoshop.admin.service.client.AdminClients.SellerAdminClient;
import com.ecoshop.admin.service.client.AdminClients.UserClient;
import com.ecoshop.admin.service.client.AdminClients.UserClient.UserView;
import com.ecoshop.admin.service.client.AdminClients.OrderClient.OrderSummary;
import com.ecoshop.admin.service.client.AdminClients.FraudAdminClient.FraudView;
import com.ecoshop.admin.service.client.AdminClients.SellerAdminClient.SellerView;
import com.ecoshop.admin.service.domain.AdminAuditLog;
import com.ecoshop.admin.service.dto.AdminDtos.*;
import com.ecoshop.admin.service.repo.AdminAuditLogRepository;
import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final AdminAuditLogRepository auditRepository;
    private final UserClient userClient;
    private final OrderClient orderClient;
    private final FraudAdminClient fraudClient;
    private final SellerAdminClient sellerClient;

    @Transactional
    public AuditLogResponse recordAudit(UUID adminUserId, String adminEmail, AuditLogRequest req) {
        AdminAuditLog entry = AdminAuditLog.builder()
                .adminUserId(adminUserId)
                .adminEmail(adminEmail)
                .action(req.action())
                .targetType(req.targetType())
                .targetId(req.targetId())
                .ipAddress(req.ipAddress())
                .details(req.details())
                .result(req.result())
                .build();
        entry = auditRepository.save(entry);
        return toAuditResponse(entry);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listAuditLogs(int page, int size) {
        Page<AdminAuditLog> result = auditRepository.findAll(
                PageRequest.of(page, size, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
        List<AuditLogResponse> content = result.getContent().stream().map(this::toAuditResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listAuditLogsForTarget(String targetType, String targetId,
                                                                  int page, int size) {
        Page<AdminAuditLog> result = auditRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                targetType, targetId, PageRequest.of(page, size));
        List<AuditLogResponse> content = result.getContent().stream().map(this::toAuditResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    /**
     * Aggregate view: user details + their order + the fraud check on that order. One call,
     * three downstream services. Used for support / fraud-review dashboards.
     */
    @Transactional(readOnly = true)
    public OrderInvestigationResponse investigateOrder(UUID orderId, String authHeader) {
        ApiResponse<OrderSummary> orderResp = orderClient.getById(authHeader, orderId);
        if (orderResp == null || !orderResp.success() || orderResp.data() == null) {
            throw BusinessException.notFound("ORDER_NOT_FOUND", "Order not found: " + orderId);
        }
        OrderSummary order = orderResp.data();

        UserView user = null;
        try {
            ApiResponse<UserView> userResp = userClient.getById(order.userId());
            if (userResp != null && userResp.success()) user = userResp.data();
        } catch (Exception e) {
            log.warn("Failed to fetch user {}: {}", order.userId(), e.getMessage());
        }

        FraudView fraud = null;
        try {
            ApiResponse<FraudView> fraudResp = fraudClient.getLatestForOrder(orderId);
            if (fraudResp != null && fraudResp.success()) fraud = fraudResp.data();
        } catch (Exception e) {
            log.info("No fraud check for order {} (or fraud-service unavailable): {}", orderId, e.getMessage());
        }

        return new OrderInvestigationResponse(
                order.id(), order.orderNumber(), order.userId(), order.status(),
                order.totalAmount(), order.currency(),
                order.paymentId(), order.paymentStatus(),
                user, fraud, order.createdAt()
        );
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(UUID userId) {
        ApiResponse<UserView> resp = userClient.getById(userId);
        if (resp == null || !resp.success() || resp.data() == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "User " + userId + " not found");
        }
        UserView u = resp.data();
        return new UserDetailResponse(
                u.id(), u.email(), u.firstName(), u.lastName(), u.phone(),
                u.role(), u.status(), u.emailVerified(), u.createdAt()
        );
    }

    @Transactional(readOnly = true)
    public Object getSellerDetail(UUID sellerId) {
        ApiResponse<SellerView> resp = sellerClient.getById(sellerId);
        if (resp == null || !resp.success() || resp.data() == null) {
            throw BusinessException.notFound("SELLER_NOT_FOUND", "Seller " + sellerId + " not found");
        }
        return resp.data();
    }

    public AuditLogResponse toAuditResponse(AdminAuditLog l) {
        return new AuditLogResponse(
                l.getId(), l.getAdminUserId(), l.getAdminEmail(),
                l.getAction(), l.getTargetType(), l.getTargetId(),
                l.getIpAddress(), l.getDetails(), l.getResult(),
                l.getCreatedAt()
        );
    }
}
