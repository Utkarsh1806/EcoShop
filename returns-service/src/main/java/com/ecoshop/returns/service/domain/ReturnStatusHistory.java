package com.ecoshop.returns.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "return_status_history", indexes = {
        @Index(name = "idx_return_history_request", columnList = "return_request_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReturnStatusHistory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    private ReturnStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 32)
    private ReturnStatus toStatus;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "changed_by", length = 100)
    private String changedBy;
}
