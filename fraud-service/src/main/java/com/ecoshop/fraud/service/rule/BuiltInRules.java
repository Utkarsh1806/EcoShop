package com.ecoshop.fraud.service.rule;

import com.ecoshop.fraud.service.dto.FraudDtos.EvaluateOrderRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Built-in fraud rules. Add new rules as @Component beans implementing FraudRule —
 * they're auto-discovered and applied in order of code() (alphabetical-ish).
 */
public class BuiltInRules {

    @Component
    public static class BlocklistEmailRule implements FraudRule {
        @Override public String code() { return "BLOCKLIST_EMAIL"; }
        @Override public String description() { return "Email is blocklisted"; }
        @Override public Optional<RuleResult> evaluate(EvaluateOrderRequest req, RuleContext ctx) {
            if (req.userEmail() == null) return Optional.empty();
            String key = "EMAIL:" + req.userEmail().toLowerCase();
            if (ctx.getBlocklistMatches().contains(key)) {
                return Optional.of(new RuleResult(80, "Email " + req.userEmail() + " on blocklist"));
            }
            return Optional.empty();
        }
    }

    @Component
    public static class BlocklistPhoneRule implements FraudRule {
        @Override public String code() { return "BLOCKLIST_PHONE"; }
        @Override public String description() { return "Phone is blocklisted"; }
        @Override public Optional<RuleResult> evaluate(EvaluateOrderRequest req, RuleContext ctx) {
            if (req.userPhone() == null) return Optional.empty();
            String key = "PHONE:" + req.userPhone();
            if (ctx.getBlocklistMatches().contains(key)) {
                return Optional.of(new RuleResult(80, "Phone " + req.userPhone() + " on blocklist"));
            }
            return Optional.empty();
        }
    }

    @Component
    public static class BlocklistIpRule implements FraudRule {
        @Override public String code() { return "BLOCKLIST_IP"; }
        @Override public String description() { return "Source IP is blocklisted"; }
        @Override public Optional<RuleResult> evaluate(EvaluateOrderRequest req, RuleContext ctx) {
            if (req.ipAddress() == null) return Optional.empty();
            String key = "IP:" + req.ipAddress();
            if (ctx.getBlocklistMatches().contains(key)) {
                return Optional.of(new RuleResult(70, "IP " + req.ipAddress() + " on blocklist"));
            }
            return Optional.empty();
        }
    }

    @Component
    public static class HighOrderValueRule implements FraudRule {
        @Override public String code() { return "HIGH_ORDER_VALUE"; }
        @Override public String description() { return "Order value above threshold"; }
        @Override public Optional<RuleResult> evaluate(EvaluateOrderRequest req, RuleContext ctx) {
            BigDecimal threshold = new BigDecimal("50000"); // ₹50,000
            if (req.totalAmount() != null && req.totalAmount().compareTo(threshold) >= 0) {
                int score = req.totalAmount().compareTo(new BigDecimal("200000")) >= 0 ? 40 : 20;
                return Optional.of(new RuleResult(score,
                        "Order total ₹" + req.totalAmount() + " ≥ threshold"));
            }
            return Optional.empty();
        }
    }

    @Component
    public static class FirstOrderHighValueRule implements FraudRule {
        @Override public String code() { return "FIRST_ORDER_HIGH_VALUE"; }
        @Override public String description() { return "First-time customer with high-value order"; }
        @Override public Optional<RuleResult> evaluate(EvaluateOrderRequest req, RuleContext ctx) {
            if (req.isFirstOrder() != null && req.isFirstOrder()
                && req.totalAmount() != null
                && req.totalAmount().compareTo(new BigDecimal("20000")) >= 0) {
                return Optional.of(new RuleResult(25,
                        "First-order amount ₹" + req.totalAmount() + " is unusually high"));
            }
            return Optional.empty();
        }
    }

    @Component
    public static class VelocityRule implements FraudRule {
        @Override public String code() { return "VELOCITY_24H"; }
        @Override public String description() { return "Too many orders in 24h"; }
        @Override public Optional<RuleResult> evaluate(EvaluateOrderRequest req, RuleContext ctx) {
            if (ctx.getChecksLast24h() >= 10) {
                return Optional.of(new RuleResult(35,
                        ctx.getChecksLast24h() + " orders by user in last 24h"));
            }
            if (ctx.getChecksLast24h() >= 5) {
                return Optional.of(new RuleResult(15,
                        ctx.getChecksLast24h() + " orders by user in last 24h"));
            }
            return Optional.empty();
        }
    }

    @Component
    public static class RecentHighRiskRule implements FraudRule {
        @Override public String code() { return "RECENT_HIGH_RISK"; }
        @Override public String description() { return "User had recent HIGH/CRITICAL fraud check"; }
        @Override public Optional<RuleResult> evaluate(EvaluateOrderRequest req, RuleContext ctx) {
            if (ctx.getHighRisk7d() > 0) {
                return Optional.of(new RuleResult(30,
                        ctx.getHighRisk7d() + " HIGH/CRITICAL checks in last 7d"));
            }
            return Optional.empty();
        }
    }

    @Component
    public static class BillingShippingMismatchRule implements FraudRule {
        @Override public String code() { return "BILLING_SHIPPING_MISMATCH"; }
        @Override public String description() { return "Billing and shipping pincodes far apart"; }
        @Override public Optional<RuleResult> evaluate(EvaluateOrderRequest req, RuleContext ctx) {
            if (req.shippingPostalCode() == null || req.billingPostalCode() == null) return Optional.empty();
            if (!req.shippingPostalCode().equals(req.billingPostalCode())) {
                int score = 5;
                try {
                    int s = Integer.parseInt(req.shippingPostalCode().replaceAll("[^0-9]", ""));
                    int b = Integer.parseInt(req.billingPostalCode().replaceAll("[^0-9]", ""));
                    // First two digits of an Indian PIN encode the state circle
                    if (Math.abs(s / 10000 - b / 10000) >= 2) score = 15;
                } catch (NumberFormatException ignored) {}
                return Optional.of(new RuleResult(score,
                        "Shipping " + req.shippingPostalCode() + " vs billing " + req.billingPostalCode()));
            }
            return Optional.empty();
        }
    }
}
