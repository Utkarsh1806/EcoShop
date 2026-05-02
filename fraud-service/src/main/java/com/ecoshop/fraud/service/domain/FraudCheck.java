package com.ecoshop.fraud.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fraud_checks", indexes = {
        @Index(name = "idx_fc_subject", columnList = "subject_type,subject_id"),
        @Index(name = "idx_fc_user", columnList = "user_id"),
        @Index(name = "idx_fc_risk", columnList = "risk_level")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FraudCheck extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "subject_type", nullable = false, length = 32)
    private String subjectType; // ORDER | PAYMENT | USER | LOGIN

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore; // 0-100

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 32)
    private RiskLevel riskLevel;

    @Column(name = "decision", nullable = false, length = 32)
    private String decision; // APPROVE | REVIEW | BLOCK

    @Column(name = "manual_review_required", nullable = false)
    @Builder.Default
    private boolean manualReviewRequired = false;

    @OneToMany(mappedBy = "fraudCheck", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RuleHit> ruleHits = new ArrayList<>();

    public void addHit(RuleHit h) { ruleHits.add(h); h.setFraudCheck(this); }
}
