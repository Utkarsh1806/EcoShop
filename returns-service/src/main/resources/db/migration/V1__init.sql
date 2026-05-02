-- returns-service schema

CREATE TABLE return_requests (
    id UUID PRIMARY KEY,
    rma_number VARCHAR(50) NOT NULL UNIQUE,
    order_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'REQUESTED',
    reason VARCHAR(32) NOT NULL,
    reason_details VARCHAR(1000),
    refund_amount NUMERIC(12,2),
    refund_payment_id UUID,
    pickup_tracking_number VARCHAR(100),
    pickup_scheduled_at TIMESTAMPTZ,
    qc_note VARCHAR(1000),
    rejection_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_returns_order ON return_requests(order_id);
CREATE INDEX idx_returns_user ON return_requests(user_id);
CREATE INDEX idx_returns_status ON return_requests(status);

CREATE TABLE return_items (
    id UUID PRIMARY KEY,
    return_request_id UUID NOT NULL REFERENCES return_requests(id) ON DELETE CASCADE,
    order_item_id UUID NOT NULL,
    product_id UUID NOT NULL,
    sku VARCHAR(100),
    product_name VARCHAR(300),
    quantity INT NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    line_refund_amount NUMERIC(12,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_return_items_request ON return_items(return_request_id);

CREATE TABLE return_status_history (
    id UUID PRIMARY KEY,
    return_request_id UUID NOT NULL REFERENCES return_requests(id) ON DELETE CASCADE,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    note VARCHAR(500),
    changed_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_return_history_request ON return_status_history(return_request_id);
