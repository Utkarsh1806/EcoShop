package com.ecoshop.fraud.service.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * Lookups the engine performs once before running rules, so rules don't each re-query.
 */
@Getter
@RequiredArgsConstructor
public class RuleContext {

    /** Distinct blocklist matches for this evaluation, e.g. EMAIL:foo@bar */
    private final Set<String> blocklistMatches;

    /** How many fraud checks the user has had in the last 24h */
    private final long checksLast24h;

    /** How many HIGH/CRITICAL outcomes in the last 7d */
    private final long highRisk7d;
}
