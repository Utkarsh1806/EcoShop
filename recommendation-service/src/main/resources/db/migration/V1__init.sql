-- recommendation-service schema baseline
-- TODO: define entities specific to Personalized recommendations (ML contract)
CREATE TABLE IF NOT EXISTS service_health_check (
    id BIGSERIAL PRIMARY KEY,
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
