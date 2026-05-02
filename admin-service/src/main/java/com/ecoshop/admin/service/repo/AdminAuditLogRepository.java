package com.ecoshop.admin.service.repo;

import com.ecoshop.admin.service.domain.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {
    Page<AdminAuditLog> findByAdminUserIdOrderByCreatedAtDesc(UUID adminUserId, Pageable pageable);
    Page<AdminAuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
    Page<AdminAuditLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            String targetType, String targetId, Pageable pageable);
}
