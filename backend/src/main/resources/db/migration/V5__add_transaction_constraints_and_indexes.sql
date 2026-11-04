CREATE FUNCTION tx_time(raw_data JSONB)
    RETURNS TIMESTAMPTZ
    LANGUAGE sql
    IMMUTABLE
    PARALLEL SAFE
AS
$$
SELECT (raw_data ->> 'transaction_at')::TIMESTAMPTZ
$$;

ALTER TABLE transaction
    ALTER COLUMN raw_data SET NOT NULL;

ALTER TABLE transaction
    ADD CONSTRAINT ck_transaction_status
        CHECK (status IN ('APPROVED', 'DECLINED', 'REFUNDED', 'CANCELLED')),
    ADD CONSTRAINT ck_transaction_raw_data_required_keys
        CHECK (raw_data ->> 'transaction_at' IS NOT NULL
            AND raw_data ->> 'type' IS NOT NULL
            AND raw_data ->> 'card_brand' IS NOT NULL
            AND raw_data ->> 'sales_channel' IS NOT NULL),
    ADD CONSTRAINT ck_transaction_raw_data_transaction_at
        CHECK (raw_data ->> 'transaction_at'
            ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}(:[0-9]{2}(\.[0-9]{1,9})?)?(Z|[+-][0-9]{2}:[0-9]{2})$'),
    ADD CONSTRAINT ck_transaction_raw_data_type
        CHECK (raw_data ->> 'type' IN ('SALE', 'REFUND', 'VOID')),
    ADD CONSTRAINT ck_transaction_raw_data_card_brand
        CHECK (raw_data ->> 'card_brand' IN ('VISA', 'MASTERCARD', 'AMEX', 'DINERS', 'MAESTRO')),
    ADD CONSTRAINT ck_transaction_raw_data_sales_channel
        CHECK (raw_data ->> 'sales_channel' IN ('POS', 'ECOM', 'MOTO', 'CONTACTLESS')),
    ADD CONSTRAINT ck_transaction_raw_data_masked_pan
        CHECK (raw_data ->> 'masked_pan' ~ '^[0-9]{6}\*{2,9}[0-9]{4}$');

CREATE INDEX ix_transaction_terminal_tx_time ON transaction (id_terminal, tx_time(raw_data) DESC);
CREATE INDEX ix_transaction_tx_time ON transaction (tx_time(raw_data) DESC);
CREATE INDEX ix_transaction_rrn ON transaction ((raw_data ->> 'rrn'));
CREATE INDEX ix_transaction_auth_code ON transaction ((raw_data ->> 'auth_code'));
CREATE INDEX ix_transaction_acquirer ON transaction (id_acquirer);
CREATE INDEX ix_transaction_raw_data ON transaction USING GIN (raw_data jsonb_path_ops);
