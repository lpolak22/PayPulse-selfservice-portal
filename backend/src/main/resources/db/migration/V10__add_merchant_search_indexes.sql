CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX ix_merchant_name_trgm ON merchant USING gin (lower(name) gin_trgm_ops);

CREATE INDEX ix_merchant_organisation_status ON merchant (id_organisation, lower(status));
