-- inventory-service schema

CREATE TABLE warehouses (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    city VARCHAR(100),
    country VARCHAR(2) DEFAULT 'IN',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE stock_items (
    id UUID PRIMARY KEY,
    warehouse_id UUID NOT NULL REFERENCES warehouses(id),
    product_id UUID NOT NULL,
    variant_id UUID,
    sku VARCHAR(100) NOT NULL,
    on_hand INT NOT NULL DEFAULT 0,
    reserved INT NOT NULL DEFAULT 0,
    low_stock_threshold INT DEFAULT 10,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_reserved_lte_on_hand CHECK (reserved <= on_hand AND reserved >= 0)
);
CREATE INDEX idx_stock_sku ON stock_items(sku);
CREATE INDEX idx_stock_product ON stock_items(product_id);
CREATE UNIQUE INDEX idx_stock_warehouse_sku ON stock_items(warehouse_id, sku);

CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    reference_id VARCHAR(100) NOT NULL UNIQUE,
    reference_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'HELD',
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_reservations_status ON reservations(status);

CREATE TABLE reservation_items (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    stock_item_id UUID NOT NULL,
    sku VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_reservation_items_resv ON reservation_items(reservation_id);
CREATE INDEX idx_reservation_items_stock ON reservation_items(stock_item_id);

-- Seed: one default warehouse for dev
INSERT INTO warehouses (id, code, name, city) VALUES
    ('11111111-1111-1111-1111-111111111111', 'BLR-CENTRAL', 'Bangalore Central DC', 'Bengaluru');
