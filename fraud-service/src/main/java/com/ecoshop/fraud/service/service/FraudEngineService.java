package com.ecoshop.fraud.service.service;

import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.fraud.service.domain.*;
import com.ecoshop.fraud.service.dto.FraudDtos.*;
import com.ecoshop.fraud.service.repo.BlocklistEntryRepository;
import com.ecoshop.fraud.service.repo.FraudCheckRepository;
import com.ecoshop.fraud.service.rule.FraudRule;
import com.ecoshop.fraud.service.rule.FraudRule.RuleResult;
import com.ecoshop.fraud.service.rule.RuleContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FraudEngineService {

    private static final Logger log = LoggerFactory.getLogger(FraudEngineService.class);
    public static final String TOPIC_FRAUD_DECISION = "fraud.decision";

    private final List<FraudRule> rules;
    private final FraudCheckRepository checkRepository;
    private final BlocklistEntryRepository blocklistRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public FraudDecisionResponse evaluateOrder(EvaluateOrderRequest req) {
        // Build context (lookups happen once)
        Set<String> blocklistMatches = new HashSet<>();
        if (req.userEmail() != null)
            blocklistRepository.findByEntryTypeAndEntryValue("EMAIL", req.userEmail().toLowerCase())
                    .ifPresent(b -> blocklistMatches.add("EMAIL:" + b.getEntryValue()));
        if (req.userPhone() != null)
            blocklistRepository.findByEntryTypeAndEntryValue("PHONE", req.userPhone())
                    .ifPresent(b -> blocklistMatches.add("PHONE:" + b.getEntryValue()));
        if (req.ipAddress() != null)
            blocklistRepository.findByEntryTypeAndEntryValue("IP", req.ipAddress())
                    .ifPresent(b -> blocklistMatches.add("IP:" + b.getEntryValue()));
        if (req.deviceId() != null)
            blocklistRepository.findByEntryTypeAndEntryValue("DEVICE_ID", req.deviceId())
                    .ifPresent(b -> blocklistMatches.add("DEVICE_ID:" + b.getEntryValue()));

        long checksLast24h = checkRepository.countByUserIdAndCreatedAtAfter(
                req.userId(), Instant.now().minus(24, ChronoUnit.HOURS));
        long highRisk7d = checkRepository.findByUserIdAndCreatedAtAfter(
                req.userId(), Instant.now().minus(7, ChronoUnit.DAYS)).stream()
                .filter(c -> c.getRiskLevel() == RiskLevel.HIGH || c.getRiskLevel() == RiskLevel.CRITICAL)
                .count();

        RuleContext ctx = new RuleContext(blocklistMatches, checksLast24h, highRisk7d);

        // Persist a check now so we capture the evaluation regardless of where rules go
        FraudCheck check = FraudCheck.builder()
                .subjectType("ORDER")
                .subjectId(req.orderId())
                .userId(req.userId())
                .build();

        int totalScore = 0;
        for (FraudRule rule : rules) {
            try {
                Optional<RuleResult> hit = rule.evaluate(req, ctx);
                if (hit.isPresent()) {
                    RuleResult r = hit.get();
                    totalScore += r.scoreAdded();
                    check.addHit(RuleHit.builder()
                            .ruleCode(rule.code())
                            .ruleDescription(rule.description())
                            .scoreAdded(r.scoreAdded())
                            .evidence(r.evidence())
                            .build());
                }
            } catch (Exception e) {
                log.warn("Rule {} threw {}: {}", rule.code(), e.getClass().getSimpleName(), e.getMessage());
            }
        }
        totalScore = Math.min(100, totalScore);

        RiskLevel level;
        String decision;
        boolean manualReview = false;
        if (totalScore >= 80) {
            level = RiskLevel.CRITICAL;
            decision = "BLOCK";
        } else if (totalScore >= 50) {
            level = RiskLevel.HIGH;
            decision = "REVIEW";
            manualReview = true;
        } else if (totalScore >= 25) {
            level = RiskLevel.MEDIUM;
            decision = "APPROVE";
        } else {
            level = RiskLevel.LOW;
            decision = "APPROVE";
        }
        check.setRiskScore(totalScore);
        check.setRiskLevel(level);
        check.setDecision(decision);
        check.setManualReviewRequired(manualReview);
        check = checkRepository.save(check);
        log.info("Fraud check {} for order {}: score={} level={} decision={} hits={}",
                check.getId(), req.orderId(), totalScore, level, decision, check.getRuleHits().size());
        publishDecision(check);
        return toResponse(check);
    }

    @Transactional(readOnly = true)
    public FraudDecisionResponse getLatestForOrder(UUID orderId) {
        FraudCheck c = checkRepository
                .findFirstBySubjectTypeAndSubjectIdOrderByCreatedAtDesc("ORDER", orderId)
                .orElseThrow(() -> BusinessException.notFound("CHECK_NOT_FOUND",
                        "No fraud check for order " + orderId));
        return toResponse(c);
    }

    // ─── Blocklist management ───

    @Transactional
    public BlocklistEntryResponse addToBlocklist(BlocklistEntryRequest req, String actor) {
        BlocklistEntry entry = blocklistRepository
                .findByEntryTypeAndEntryValue(req.entryType(), req.entryValue())
                .orElseGet(() -> BlocklistEntry.builder()
                        .entryType(req.entryType())
                        .entryValue(req.entryValue())
                        .build());
        entry.setReason(req.reason());
        entry.setAddedBy(actor);
        entry.setExpiresAt(req.expiresAt());
        entry = blocklistRepository.save(entry);
        log.info("Blocklist entry {} added by {}: {}={}",
                entry.getId(), actor, entry.getEntryType(), entry.getEntryValue());
        return toBlocklistResponse(entry);
    }

    @Transactional
    public void removeFromBlocklist(UUID id) {
        blocklistRepository.deleteById(id);
    }

    private void publishDecision(FraudCheck c) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventId", UUID.randomUUID().toString());
        evt.put("checkId", c.getId().toString());
        evt.put("subjectType", c.getSubjectType());
        evt.put("subjectId", c.getSubjectId().toString());
        if (c.getUserId() != null) evt.put("userId", c.getUserId().toString());
        evt.put("riskScore", c.getRiskScore());
        evt.put("riskLevel", c.getRiskLevel().name());
        evt.put("decision", c.getDecision());
        evt.put("manualReviewRequired", c.isManualReviewRequired());
        kafkaTemplate.send(TOPIC_FRAUD_DECISION, c.getSubjectId().toString(), evt);
    }

    public FraudDecisionResponse toResponse(FraudCheck c) {
        List<RuleHitDto> hits = c.getRuleHits().stream()
                .map(h -> new RuleHitDto(h.getRuleCode(), h.getRuleDescription(),
                        h.getScoreAdded(), h.getEvidence()))
                .toList();
        return new FraudDecisionResponse(
                c.getId(), c.getSubjectType(), c.getSubjectId(), c.getUserId(),
                c.getRiskScore(), c.getRiskLevel(), c.getDecision(),
                c.isManualReviewRequired(), hits, c.getCreatedAt()
        );
    }

    public BlocklistEntryResponse toBlocklistResponse(BlocklistEntry e) {
        return new BlocklistEntryResponse(
                e.getId(), e.getEntryType(), e.getEntryValue(),
                e.getReason(), e.getAddedBy(), e.getExpiresAt()
        );
    }
}
