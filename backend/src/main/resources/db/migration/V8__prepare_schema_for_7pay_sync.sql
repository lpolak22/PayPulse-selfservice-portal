ALTER TABLE transaction
    DROP CONSTRAINT ck_transaction_raw_data_required_keys,
    DROP CONSTRAINT ck_transaction_raw_data_type,
    DROP CONSTRAINT ck_transaction_raw_data_card_brand;

UPDATE transaction
SET raw_data = jsonb_set(raw_data, '{type}', '"STORNO"')
WHERE raw_data ->> 'type' = 'VOID';

ALTER TABLE transaction
    ADD CONSTRAINT ck_transaction_raw_data_required_keys
        CHECK (raw_data ->> 'transaction_at' IS NOT NULL
            AND raw_data ->> 'type' IS NOT NULL
            AND raw_data ->> 'card_brand' IS NOT NULL),
    ADD CONSTRAINT ck_transaction_raw_data_type
        CHECK (raw_data ->> 'type' IN ('SALE', 'SALE_WITH_INSTALLMENTS', 'REFUND', 'STORNO',
                                       'TECHNICAL_REVERSAL')),
    ADD CONSTRAINT ck_transaction_raw_data_card_brand
        CHECK (raw_data ->> 'card_brand' IN ('VISA', 'MASTERCARD', 'MAESTRO', 'DINERS', 'DISCOVER', 'JCB',
                                             'AMEX', 'CLOSED_LOOP_PREPAID', 'CLOSED_LOOP_POSTPAID',
                                             'NAKA', 'NOT_KNOWN'));

ALTER TABLE outlet
    ADD COLUMN country VARCHAR(3);

CREATE TABLE sync_run
(
    id_sync_run    BIGSERIAL PRIMARY KEY,
    started_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at    TIMESTAMPTZ,
    status         VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    rows_processed INTEGER     NOT NULL DEFAULT 0,
    rows_skipped   INTEGER     NOT NULL DEFAULT 0,
    rows_failed    INTEGER     NOT NULL DEFAULT 0,
    error_message  TEXT,
    CONSTRAINT ck_sync_run_status CHECK (status IN ('RUNNING', 'SUCCESS', 'PARTIAL', 'FAILED')),
    CONSTRAINT ck_sync_run_rows CHECK (rows_processed >= 0 AND rows_skipped >= 0 AND rows_failed >= 0),
    CONSTRAINT ck_sync_run_finished CHECK (status = 'RUNNING' OR finished_at IS NOT NULL)
);

CREATE INDEX ix_sync_run_started_at ON sync_run (started_at DESC);
