ALTER TABLE terminal DROP CONSTRAINT fk_terminal_mcc;

ALTER TABLE mcc_code ALTER COLUMN mcc_code TYPE VARCHAR(9);
ALTER TABLE terminal ALTER COLUMN mcc_code TYPE VARCHAR(9);

ALTER TABLE terminal
    ADD CONSTRAINT fk_terminal_mcc FOREIGN KEY (mcc_code) REFERENCES mcc_code (mcc_code);

ALTER TABLE mcc_code
    ADD CONSTRAINT ck_mcc_code_format CHECK (mcc_code ~ '^[0-9]{4}(-[0-9]{4})?$');
