-- payment-service schema

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    user_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    method VARCHAR(20) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    gateway VARCHAR(50),
    gateway_ref VARCHAR(100),
    idempotency_key VARCHAR(100) UNIQUE,
    amount_refunded NUMERIC(12,2) DEFAULT 0,
    failure_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_payments_gateway_ref ON payments(gateway_ref);
CREATE INDEX idx_payments_status ON payments(status);
