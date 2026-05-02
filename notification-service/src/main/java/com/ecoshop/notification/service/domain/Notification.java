package com.ecoshop.notification.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_user", columnList = "user_id"),
        @Index(name = "idx_notif_status", columnList = "status"),
        @Index(name = "idx_notif_dedupe", columnList = "dedupe_key", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;

    @Column(nullable = false, length = 500)
    private String recipient; // email, phone, deviceToken

    @Column(length = 500)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "template_key", length = 100)
    private String templateKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.QUEUED;

    /** Used for idempotent delivery (e.g. orderId + event type) */
    @Column(name = "dedupe_key", length = 200, unique = true)
    private String dedupeKey;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;
}
