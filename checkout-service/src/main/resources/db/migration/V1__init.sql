-- checkout-service schema

CREATE TABLE checkout_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    cart_key VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'STARTED',
    idempotency_key VARCHAR(100) UNIQUE,
    subtotal NUMERIC(12,2),
    discount NUMERIC(12,2),
    tax NUMERIC(12,2),
    shipping_cost NUMERIC(12,2),
    total_amount NUMERIC(12,2),
    currency VARCHAR(3) DEFAULT 'INR',
    coupon_code VARCHAR(50),
    reservation_id UUID,
    order_id UUID,
    order_number VARCHAR(50),
    payment_id UUID,
    payment_gateway_ref VARCHAR(100),
    client_secret VARCHAR(200),
    checkout_url VARCHAR(500),
    failure_reason VARCHAR(1000),
    -- Shipping address snapshot
    ship_recipient_name VARCHAR(200),
    ship_phone VARCHAR(20),
    ship_line1 VARCHAR(255),
    ship_line2 VARCHAR(255),
    ship_city VARCHAR(100),
    ship_state VARCHAR(100),
    ship_postal_code VARCHAR(20),
    ship_country VARCHAR(2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_checkout_user ON checkout_sessions(user_id);
CREATE INDEX idx_checkout_status ON checkout_sessions(status);
