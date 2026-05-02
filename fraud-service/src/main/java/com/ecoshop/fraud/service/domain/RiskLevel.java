package com.ecoshop.fraud.service.domain;

public enum RiskLevel {
    LOW,        // pass
    MEDIUM,     // pass with monitoring
    HIGH,       // require manual review
    CRITICAL    // block automatically
}
