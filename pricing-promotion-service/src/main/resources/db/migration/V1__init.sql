-- pricing-promotion-service schema

CREATE TABLE coupons (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    discount_type VARCHAR(20) NOT NULL,
    discount_value NUMERIC(12,2) NOT NULL,
    max_discount_amount NUMERIC(12,2),
    min_cart_amount NUMERIC(12,2) DEFAULT 0,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    usage_limit INT,
    usage_count INT NOT NULL DEFAULT 0,
    per_user_limit INT DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE coupon_redemptions (
    id UUID PRIMARY KEY,
    coupon_id UUID NOT NULL REFERENCES coupons(id),
    user_id UUID NOT NULL,
    order_id UUID NOT NULL UNIQUE,
    discount_applied NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_redemptions_coupon ON coupon_redemptions(coupon_id);
CREATE INDEX idx_redemptions_user ON coupon_redemptions(user_id);

-- Seed: a demo coupon valid for 1 year
INSERT INTO coupons (id, code, description, discount_type, discount_value, max_discount_amount,
                     min_cart_amount, valid_from, valid_until, usage_limit, per_user_limit)
VALUES ('22222222-2222-2222-2222-222222222222',
        'WELCOME10', 'Welcome 10% off (max ₹500)',
        'PERCENTAGE', 10, 500, 500,
        NOW() - INTERVAL '1 day', NOW() + INTERVAL '365 days',
        10000, 1);
