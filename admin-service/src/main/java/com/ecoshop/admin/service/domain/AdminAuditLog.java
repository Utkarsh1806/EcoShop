package com.ecoshop.admin.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "admin_audit_logs", indexes = {
        @Index(name = "idx_audit_admin", columnList = "admin_user_id"),
        @Index(name = "idx_audit_target", columnList = "target_type,target_id"),
        @Index(name = "idx_audit_action", columnList = "action")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AdminAuditLog extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "admin_user_id", nullable = false)
    private UUID adminUserId;

    @Column(name = "admin_email", length = 255)
    private String adminEmail;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id", length = 200)
    private String targetId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "result", length = 20)
    private String result; // SUCCESS | FAILURE
}
