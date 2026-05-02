-- shipping-service schema

CREATE TABLE shipments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    courier VARCHAR(50) NOT NULL,
    tracking_number VARCHAR(100),
    label_url VARCHAR(1000),
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    shipping_cost NUMERIC(12,2),
    weight_grams INT,
    estimated_delivery TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    -- Destination snapshot
    to_recipient_name VARCHAR(200),
    to_phone VARCHAR(20),
    to_line1 VARCHAR(255),
    to_line2 VARCHAR(255),
    to_city VARCHAR(100),
    to_state VARCHAR(100),
    to_postal_code VARCHAR(20),
    to_country VARCHAR(2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_shipments_tracking ON shipments(tracking_number);
CREATE INDEX idx_shipments_status ON shipments(status);

CREATE TABLE tracking_events (
    id UUID PRIMARY KEY,
    shipment_id UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    location VARCHAR(200),
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_tracking_shipment ON tracking_events(shipment_id);
