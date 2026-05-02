package com.ecoshop.fraud.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blocklist_entries", indexes = {
        @Index(name = "idx_blocklist_lookup", columnList = "entry_type,entry_value", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BlocklistEntry extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entry_type", nullable = false, length = 32)
    private String entryType; // EMAIL | PHONE | IP | CARD_BIN | DEVICE_ID | USER_ID

    @Column(name = "entry_value", nullable = false, length = 200)
    private String entryValue;

    @Column(length = 500)
    private String reason;

    @Column(name = "added_by", length = 100)
    private String addedBy;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
