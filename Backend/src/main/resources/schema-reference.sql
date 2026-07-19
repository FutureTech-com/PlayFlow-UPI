-- =====================================================================
-- PayFlow — Reference Database Schema
-- =====================================================================
-- This mirrors exactly what Hibernate auto-generates from the JPA
-- entities under backend/src/main/java/com/payflow/entity when running
-- with ddl-auto=update/create-drop. You don't need to run this by hand
-- in dev (H2) — it's here for:
--   1. Documentation / ER reference
--   2. Manual provisioning of a production Postgres or MySQL database
--      where you'd rather review DDL than trust auto-DDL
--
-- Written for PostgreSQL. MySQL differences are called out inline.
-- All primary keys are UUID strings (36 chars) to match
-- GenerationType.UUID used on BaseEntity.
-- =====================================================================

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                    VARCHAR(36)     PRIMARY KEY,
    created_at            TIMESTAMP       NOT NULL,
    updated_at            TIMESTAMP,
    full_name             VARCHAR(255)    NOT NULL,
    email                 VARCHAR(255)    NOT NULL UNIQUE,
    phone                 VARCHAR(20)     NOT NULL UNIQUE,
    password_hash         VARCHAR(255)    NOT NULL,
    transaction_pin_hash  VARCHAR(255),
    pin_set               BOOLEAN         NOT NULL DEFAULT FALSE,
    enabled               BOOLEAN         NOT NULL DEFAULT TRUE,
    phone_verified        BOOLEAN         NOT NULL DEFAULT FALSE,
    email_verified        BOOLEAN         NOT NULL DEFAULT FALSE
);

-- roles (element collection on User — one row per role per user)
CREATE TABLE user_roles (
    user_id  VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role     VARCHAR(20)  NOT NULL CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- ---------------------------------------------------------------------
-- bank_accounts
-- ---------------------------------------------------------------------
CREATE TABLE bank_accounts (
    id                   VARCHAR(36)     PRIMARY KEY,
    created_at           TIMESTAMP       NOT NULL,
    updated_at           TIMESTAMP,
    user_id              VARCHAR(36)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bank_name            VARCHAR(255)    NOT NULL,
    account_holder_name  VARCHAR(255)    NOT NULL,
    account_number       VARCHAR(34)     NOT NULL,
    ifsc_code            VARCHAR(11)     NOT NULL,
    balance              NUMERIC(15,2)   NOT NULL DEFAULT 10000.00,
    primary_account      BOOLEAN         NOT NULL DEFAULT FALSE,  -- column name "primary" is reserved in some DBs
    verified             BOOLEAN         NOT NULL DEFAULT FALSE,
    account_type         VARCHAR(10)     NOT NULL DEFAULT 'SAVINGS' CHECK (account_type IN ('SAVINGS', 'CURRENT'))
);
CREATE INDEX idx_bank_accounts_user_id ON bank_accounts(user_id);

-- ---------------------------------------------------------------------
-- upi_ids
-- ---------------------------------------------------------------------
CREATE TABLE upi_ids (
    id                VARCHAR(36)     PRIMARY KEY,
    created_at        TIMESTAMP       NOT NULL,
    updated_at        TIMESTAMP,
    user_id           VARCHAR(36)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bank_account_id   VARCHAR(36)     NOT NULL REFERENCES bank_accounts(id) ON DELETE CASCADE,
    vpa               VARCHAR(255)    NOT NULL UNIQUE,           -- e.g. rahul.sharma@payflow
    active            BOOLEAN         NOT NULL DEFAULT TRUE,
    qr_code_base64    TEXT                                        -- base64 PNG, generated via ZXing
);
CREATE INDEX idx_upi_ids_user_id ON upi_ids(user_id);

-- ---------------------------------------------------------------------
-- transactions
-- ---------------------------------------------------------------------
CREATE TABLE transactions (
    id               VARCHAR(36)     PRIMARY KEY,
    created_at       TIMESTAMP       NOT NULL,
    updated_at       TIMESTAMP,
    reference_id     VARCHAR(64)     NOT NULL UNIQUE,             -- e.g. PFL2026071012345678
    sender_id        VARCHAR(36)     REFERENCES users(id),
    receiver_id      VARCHAR(36)     REFERENCES users(id),
    sender_vpa       VARCHAR(255),
    receiver_vpa     VARCHAR(255),
    amount           NUMERIC(15,2)   NOT NULL,
    note             VARCHAR(500),
    type             VARCHAR(20)     NOT NULL CHECK (type IN ('SEND', 'RECEIVE', 'REQUEST', 'WALLET_ADD', 'WALLET_TO_BANK')),
    status            VARCHAR(20)     NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED')),
    failure_reason    VARCHAR(500)
);
CREATE INDEX idx_txn_sender    ON transactions(sender_id);
CREATE INDEX idx_txn_receiver  ON transactions(receiver_id);
-- idx_txn_ref is implied by the UNIQUE constraint on reference_id above.

-- ---------------------------------------------------------------------
-- beneficiaries
-- ---------------------------------------------------------------------
CREATE TABLE beneficiaries (
    id          VARCHAR(36)     PRIMARY KEY,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP,
    owner_id    VARCHAR(36)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    nickname    VARCHAR(255)    NOT NULL,
    vpa         VARCHAR(255)    NOT NULL,
    favourite   BOOLEAN         NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_beneficiaries_owner_id ON beneficiaries(owner_id);

-- ---------------------------------------------------------------------
-- payment_requests
-- ---------------------------------------------------------------------
CREATE TABLE payment_requests (
    id                        VARCHAR(36)     PRIMARY KEY,
    created_at                TIMESTAMP       NOT NULL,
    updated_at                TIMESTAMP,
    requester_id              VARCHAR(36)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    payer_id                  VARCHAR(36)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount                    NUMERIC(15,2)   NOT NULL,
    note                      VARCHAR(500),
    status                    VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED')),
    fulfilled_transaction_id  VARCHAR(36)
);
CREATE INDEX idx_payment_requests_requester ON payment_requests(requester_id);
CREATE INDEX idx_payment_requests_payer     ON payment_requests(payer_id);

-- ---------------------------------------------------------------------
-- notifications
-- ---------------------------------------------------------------------
CREATE TABLE notifications (
    id          VARCHAR(36)     PRIMARY KEY,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP,
    user_id     VARCHAR(36)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(255)    NOT NULL,
    message     VARCHAR(1000)   NOT NULL,
    type        VARCHAR(30)     NOT NULL CHECK (type IN (
                     'PAYMENT_SUCCESS', 'PAYMENT_FAILED', 'REQUEST_RECEIVED',
                     'REQUEST_ACCEPTED', 'REQUEST_DECLINED', 'ACCOUNT_LINKED',
                     'SECURITY_ALERT', 'GENERAL')),
    read        BOOLEAN         NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);

-- ---------------------------------------------------------------------
-- otp_verifications
-- ---------------------------------------------------------------------
CREATE TABLE otp_verifications (
    id          VARCHAR(36)     PRIMARY KEY,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP,
    identifier  VARCHAR(255)    NOT NULL,      -- email or phone
    otp_hash    VARCHAR(255)    NOT NULL,
    purpose     VARCHAR(30)     NOT NULL CHECK (purpose IN (
                     'FORGOT_PASSWORD', 'PHONE_VERIFICATION',
                     'EMAIL_VERIFICATION', 'TRANSACTION_CONFIRM')),
    expires_at  TIMESTAMP       NOT NULL,
    used        BOOLEAN         NOT NULL DEFAULT FALSE,
    attempts    INTEGER         NOT NULL DEFAULT 0
);
CREATE INDEX idx_otp_identifier_purpose ON otp_verifications(identifier, purpose);

-- ---------------------------------------------------------------------
-- audit_logs
-- ---------------------------------------------------------------------
CREATE TABLE audit_logs (
    id          VARCHAR(36)     PRIMARY KEY,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP,
    user_id     VARCHAR(36),                    -- nullable: unauthenticated attempts still logged
    action      VARCHAR(100)    NOT NULL,        -- e.g. LOGIN_SUCCESS, LOGIN_FAILED, MONEY_SENT, PIN_CHANGED
    entity_type VARCHAR(100),
    entity_id   VARCHAR(36),
    ip_address  VARCHAR(45),
    details     TEXT
);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

-- =====================================================================
-- MySQL notes if you're deploying with the `mysql` profile instead:
--   * NUMERIC(15,2)   -> DECIMAL(15,2)
--   * TEXT for qr_code_base64/details is fine as-is (MySQL TEXT ~64KB;
--     use LONGTEXT if QR PNGs ever exceed that as base64)
--   * CHECK constraints are enforced from MySQL 8.0.16+; on older
--     versions they're parsed but silently ignored — validate status/
--     type/role values in the application layer as a fallback (already
--     done here via Java enums on the entities)
--   * `read` is a MySQL reserved word if unquoted in some contexts;
--     Hibernate will quote it automatically, but if writing raw SQL,
--     wrap it in backticks: `read`
-- =====================================================================
