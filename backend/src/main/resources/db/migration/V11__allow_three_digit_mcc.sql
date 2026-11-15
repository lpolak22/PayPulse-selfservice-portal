ALTER TABLE mcc_code DROP CONSTRAINT ck_mcc_code_format;

ALTER TABLE mcc_code
    ADD CONSTRAINT ck_mcc_code_format CHECK (mcc_code ~ '^[0-9]{3,4}(-[0-9]{3,4})?$');
