-- product-catalog-service schema

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    description VARCHAR(1000),
    parent_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_categories_parent ON categories(parent_id);

CREATE TABLE brands (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    logo_url VARCHAR(500),
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(300) NOT NULL,
    slug VARCHAR(300) NOT NULL UNIQUE,
    description TEXT,
    short_description VARCHAR(500),
    category_id UUID NOT NULL,
    brand_id UUID,
    base_price NUMERIC(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    rating_avg NUMERIC(3,2) DEFAULT 0,
    rating_count INT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_brand ON products(brand_id);
CREATE INDEX idx_products_status ON products(status);

CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL UNIQUE,
    size VARCHAR(100),
    color VARCHAR(50),
    storage VARCHAR(100),
    price NUMERIC(12,2),
    compare_at_price NUMERIC(12,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_variants_product ON product_variants(product_id);

CREATE TABLE product_images (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url VARCHAR(1000) NOT NULL,
    alt_text VARCHAR(200),
    display_order INT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_images_product ON product_images(product_id);
