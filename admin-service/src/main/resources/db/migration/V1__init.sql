-- admin-service schema

CREATE TABLE admin_audit_logs (
    id UUID PRIMARY KEY,
    admin_user_id UUID NOT NULL,
    admin_email VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(200),
    ip_address VARCHAR(45),
    details TEXT,
    result VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_audit_admin ON admin_audit_logs(admin_user_id);
CREATE INDEX idx_audit_target ON admin_audit_logs(target_type, target_id);
CREATE INDEX idx_audit_action ON admin_audit_logs(action);
