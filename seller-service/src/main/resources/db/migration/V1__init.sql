-- seller-service schema

CREATE TABLE sellers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    legal_name VARCHAR(300) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20),
    gstin VARCHAR(15) UNIQUE,
    pan VARCHAR(10),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    commission_rate NUMERIC(5,4) NOT NULL DEFAULT 0.1000,
    bank_account_holder VARCHAR(200),
    bank_account_last4 VARCHAR(4),
    bank_ifsc VARCHAR(11),
    rejection_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_sellers_legal_name ON sellers(legal_name);
CREATE INDEX idx_sellers_status ON sellers(status);

CREATE TABLE seller_product_links (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL REFERENCES sellers(id),
    product_id UUID NOT NULL UNIQUE,
    approval_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    approval_note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_spl_seller ON seller_product_links(seller_id);
CREATE INDEX idx_spl_status ON seller_product_links(approval_status);

CREATE TABLE payouts (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL REFERENCES sellers(id),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    gross_sales NUMERIC(14,2) NOT NULL,
    commission NUMERIC(14,2) NOT NULL,
    refunds NUMERIC(14,2) DEFAULT 0,
    net_payout NUMERIC(14,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    settled_at TIMESTAMPTZ,
    external_ref VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_payouts_seller ON payouts(seller_id);
CREATE INDEX idx_payouts_status ON payouts(status);
CREATE INDEX idx_payouts_period ON payouts(period_start, period_end);
