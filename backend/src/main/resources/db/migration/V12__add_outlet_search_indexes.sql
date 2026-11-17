CREATE INDEX ix_outlet_merchant_name ON outlet (id_merchant, name);

CREATE INDEX ix_outlet_merchant_city ON outlet (id_merchant, city);

UPDATE outlet
SET is_active = (status = 'ACTIVE')
WHERE is_active IS DISTINCT FROM (status = 'ACTIVE');
