package com.ecoshop.fraud.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rule_hits", indexes = {
        @Index(name = "idx_rule_hits_check", columnList = "fraud_check_id"),
        @Index(name = "idx_rule_hits_rule", columnList = "rule_code")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RuleHit extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraud_check_id", nullable = false)
    private FraudCheck fraudCheck;

    @Column(name = "rule_code", nullable = false, length = 50)
    private String ruleCode;

    @Column(name = "rule_description", length = 200)
    private String ruleDescription;

    @Column(name = "score_added", nullable = false)
    private int scoreAdded;

    @Column(name = "evidence", length = 500)
    private String evidence;
}
