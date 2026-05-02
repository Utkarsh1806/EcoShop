package com.ecoshop.fraud.service.rule;

import com.ecoshop.fraud.service.dto.FraudDtos.EvaluateOrderRequest;

import java.util.Optional;

/**
 * A rule evaluates an order context and optionally returns a hit (with score and evidence).
 * Rules are stateless and side-effect-free; they receive context and return a decision.
 */
public interface FraudRule {

    String code();

    String description();

    Optional<RuleResult> evaluate(EvaluateOrderRequest req, RuleContext ctx);

    record RuleResult(int scoreAdded, String evidence) {}
}
