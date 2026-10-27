CREATE TABLE organisation
(
    id_organisation BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_user
(
    id_user            BIGSERIAL PRIMARY KEY,
    id_organisation    BIGINT       NOT NULL,
    first_name         VARCHAR(50)  NOT NULL,
    last_name          VARCHAR(50)  NOT NULL,
    email              VARCHAR(254) NOT NULL,
    phone              VARCHAR(16),
    role               VARCHAR(50)  NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    password_hash      CHAR(60),
    failed_login_count SMALLINT     NOT NULL DEFAULT 0,
    locked_until       TIMESTAMP,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at         TIMESTAMP,
    is_active          BOOLEAN      NOT NULL DEFAULT false,
    totp_enabled       BOOLEAN      NOT NULL DEFAULT false,
    totp_secret        VARCHAR(255),
    is_first_login     BOOLEAN      NOT NULL DEFAULT true,
    CONSTRAINT fk_user_organisation FOREIGN KEY (id_organisation)
        REFERENCES organisation (id_organisation) ON DELETE CASCADE,
    CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'USER')),
    CONSTRAINT ck_app_user_status CHECK (status IN ('PENDING_ACTIVATION', 'ACTIVE', 'INACTIVE'))
);

CREATE UNIQUE INDEX ux_app_user_email_lower ON app_user (lower(email)) WHERE deleted_at IS NULL;

CREATE INDEX ix_app_user_organisation ON app_user (id_organisation);
