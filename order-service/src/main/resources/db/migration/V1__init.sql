-- order-service schema

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAYMENT',
    subtotal NUMERIC(12,2) NOT NULL,
    discount NUMERIC(12,2) DEFAULT 0,
    tax NUMERIC(12,2) DEFAULT 0,
    shipping_cost NUMERIC(12,2) DEFAULT 0,
    total_amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    coupon_code VARCHAR(50),
    payment_id VARCHAR(100),
    payment_status VARCHAR(32),
    -- Embedded shipping address
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
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    variant_id UUID,
    sku VARCHAR(100),
    product_name VARCHAR(300) NOT NULL,
    thumbnail_url VARCHAR(1000),
    unit_price NUMERIC(12,2) NOT NULL,
    quantity INT NOT NULL,
    line_total NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_order_items_order ON order_items(order_id);

CREATE TABLE order_status_history (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    note VARCHAR(500),
    changed_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_status_history_order ON order_status_history(order_id);
