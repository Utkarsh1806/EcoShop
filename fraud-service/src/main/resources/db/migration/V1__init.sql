-- fraud-service schema

CREATE TABLE fraud_checks (
    id UUID PRIMARY KEY,
    subject_type VARCHAR(32) NOT NULL,
    subject_id UUID NOT NULL,
    user_id UUID,
    risk_score INT NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    decision VARCHAR(32) NOT NULL,
    manual_review_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_fc_subject ON fraud_checks(subject_type, subject_id);
CREATE INDEX idx_fc_user ON fraud_checks(user_id);
CREATE INDEX idx_fc_risk ON fraud_checks(risk_level);

CREATE TABLE rule_hits (
    id UUID PRIMARY KEY,
    fraud_check_id UUID NOT NULL REFERENCES fraud_checks(id) ON DELETE CASCADE,
    rule_code VARCHAR(50) NOT NULL,
    rule_description VARCHAR(200),
    score_added INT NOT NULL,
    evidence VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_rule_hits_check ON rule_hits(fraud_check_id);
CREATE INDEX idx_rule_hits_rule ON rule_hits(rule_code);

CREATE TABLE blocklist_entries (
    id UUID PRIMARY KEY,
    entry_type VARCHAR(32) NOT NULL,
    entry_value VARCHAR(200) NOT NULL,
    reason VARCHAR(500),
    added_by VARCHAR(100),
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX idx_blocklist_lookup ON blocklist_entries(entry_type, entry_value);
