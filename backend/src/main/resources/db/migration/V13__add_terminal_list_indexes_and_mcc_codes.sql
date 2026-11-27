CREATE INDEX IF NOT EXISTS ix_terminal_outlet ON terminal (id_outlet);

CREATE INDEX IF NOT EXISTS ix_terminal_status ON terminal (status);

INSERT INTO mcc_code (mcc_code, description)
VALUES ('5411', 'Grocery Stores, Supermarkets'),
       ('5812', 'Eating Places, Restaurants'),
       ('5541', 'Service Stations'),
       ('5999', 'Miscellaneous Retail Stores'),
       ('5732', 'Electronics Stores')
ON CONFLICT (mcc_code) DO UPDATE SET description = EXCLUDED.description
WHERE mcc_code.description LIKE 'MCC %';
