CREATE TABLE audit_log
(
    id_audit_log BIGSERIAL PRIMARY KEY,
    id_user      BIGINT,
    action       VARCHAR(50) NOT NULL,
    entity_type  VARCHAR(30) NOT NULL,
    entity_id    BIGINT,
    old_value    JSONB,
    new_value    JSONB,
    ip_address   INET,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (id_user) REFERENCES app_user (id_user)
        ON DELETE SET NULL
);

CREATE INDEX ix_audit_log_entity ON audit_log (entity_type, entity_id);
