CREATE TABLE unknown_login_attempt
(
    email_hash         VARCHAR(64) PRIMARY KEY,
    failed_login_count SMALLINT  NOT NULL DEFAULT 0,
    locked_until       TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ix_unknown_login_attempt_updated_at ON unknown_login_attempt (updated_at);
