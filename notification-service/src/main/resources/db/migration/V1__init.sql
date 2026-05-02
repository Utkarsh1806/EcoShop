-- notification-service schema

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(500) NOT NULL,
    subject VARCHAR(500),
    body TEXT NOT NULL,
    template_key VARCHAR(100),
    status VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
    dedupe_key VARCHAR(200) UNIQUE,
    sent_at TIMESTAMPTZ,
    failure_reason VARCHAR(1000),
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_notif_user ON notifications(user_id);
CREATE INDEX idx_notif_status ON notifications(status);
