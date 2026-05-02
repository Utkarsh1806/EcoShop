-- review-rating-service schema

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    user_id UUID NOT NULL,
    order_id UUID,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(200),
    body TEXT,
    verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_MODERATION',
    helpful_count INT NOT NULL DEFAULT 0,
    moderation_note VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_reviews_status ON reviews(status);
CREATE UNIQUE INDEX idx_reviews_user_product ON reviews(user_id, product_id);

CREATE TABLE product_rating_summaries (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL UNIQUE,
    rating_count INT NOT NULL DEFAULT 0,
    rating_sum BIGINT NOT NULL DEFAULT 0,
    rating_avg NUMERIC(3,2) NOT NULL DEFAULT 0,
    count_1 INT NOT NULL DEFAULT 0,
    count_2 INT NOT NULL DEFAULT 0,
    count_3 INT NOT NULL DEFAULT 0,
    count_4 INT NOT NULL DEFAULT 0,
    count_5 INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
