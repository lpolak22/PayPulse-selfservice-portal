ALTER TABLE transaction
    ADD COLUMN transaction_at TIMESTAMPTZ GENERATED ALWAYS AS (tx_time(raw_data)) STORED;

CREATE INDEX ix_transaction_terminal_transaction_at ON transaction (id_terminal, transaction_at);
CREATE INDEX ix_transaction_transaction_at ON transaction (transaction_at);

ANALYZE transaction;
