CREATE TABLE merchant
(
    id_merchant         BIGSERIAL PRIMARY KEY,
    id_organisation     BIGINT       NOT NULL,
    source_id           VARCHAR(50) UNIQUE,
    name                VARCHAR(150) NOT NULL,
    registration_number CHAR(11) UNIQUE NOT NULL, -- OIB
    status              VARCHAR(20)  NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_merchant_organisation FOREIGN KEY (id_organisation)
        REFERENCES organisation (id_organisation) ON DELETE CASCADE
);

CREATE TABLE outlet
(
    id_outlet   BIGSERIAL PRIMARY KEY,
    id_merchant BIGINT       NOT NULL,
    source_id   VARCHAR(50) UNIQUE,
    name        VARCHAR(150) NOT NULL,
    address     VARCHAR(255),
    city        VARCHAR(100),
    postal_code VARCHAR(10),
    phone       VARCHAR(16),
    email       VARCHAR(254),
    status      VARCHAR(20)  NOT NULL,
    is_active   BOOLEAN   DEFAULT true,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_outlet_merchant FOREIGN KEY (id_merchant)
        REFERENCES merchant (id_merchant) ON DELETE CASCADE
);

CREATE TABLE mcc_code
(
    mcc_code    VARCHAR(4) PRIMARY KEY,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE terminal
(
    id_terminal    BIGSERIAL PRIMARY KEY,
    id_outlet      BIGINT      NOT NULL,
    mcc_code       CHAR(4)     NOT NULL,
    source_id      VARCHAR(50) UNIQUE,
    terminal_id    VARCHAR(16) UNIQUE NOT NULL,
    type           VARCHAR(30) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    activated_at   TIMESTAMP,
    deactivated_at TIMESTAMP,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_terminal_outlet FOREIGN KEY (id_outlet)
        REFERENCES outlet (id_outlet) ON DELETE CASCADE,
    CONSTRAINT fk_terminal_mcc FOREIGN KEY (mcc_code)
        REFERENCES mcc_code (mcc_code)
);

CREATE TABLE terminal_status_history
(
    id_status_history BIGSERIAL PRIMARY KEY,
    id_terminal       BIGINT      NOT NULL,
    status            VARCHAR(20) NOT NULL,
    changed_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    source            VARCHAR(20),
    CONSTRAINT fk_history_terminal FOREIGN KEY (id_terminal)
        REFERENCES terminal (id_terminal) ON DELETE CASCADE
);

CREATE TABLE acquirer
(
    id_acquirer BIGSERIAL PRIMARY KEY,
    code        VARCHAR(20) UNIQUE NOT NULL,
    name        VARCHAR(100)       NOT NULL
);

CREATE TABLE refresh_token
(
    id_token   BIGSERIAL PRIMARY KEY,
    id_user    BIGINT   NOT NULL,
    token_hash CHAR(64) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at    TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_user FOREIGN KEY (id_user)
        REFERENCES app_user (id_user) ON DELETE CASCADE
);

CREATE TABLE transaction
(
    id_transaction BIGSERIAL PRIMARY KEY,
    id_terminal    BIGINT         NOT NULL,
    id_acquirer    BIGINT         NOT NULL,
    source_id      VARCHAR(50) UNIQUE,
    amount         NUMERIC(12, 2) NOT NULL,
    currency       CHAR(3)        NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    raw_data       JSONB,
    CONSTRAINT fk_transaction_terminal FOREIGN KEY (id_terminal)
        REFERENCES terminal (id_terminal),
    CONSTRAINT fk_transaction_acquirer FOREIGN KEY (id_acquirer)
        REFERENCES acquirer (id_acquirer)
);
