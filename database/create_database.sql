-- ============================================================================
-- Personal Expense Tracker - PostgreSQL Database Setup
-- ============================================================================
-- Usage:
--   psql -U postgres -f create_database.sql
--
-- The Spring Boot app uses spring.jpa.hibernate.ddl-auto=update, so tables are
-- created automatically on first run. Keep every column type here in sync with
-- the JPA entities (e.g. enum STRING columns map to varchar(255)) so that
-- "update" is a no-op and never has to ALTER a column referenced by the view.
--
-- NOTE: this is the "v2" schema (users / categories / transactions).
-- This script RESETS the schema: leftover objects from v1 (expenses, old
-- categories) and any previous v2 tables are dropped first. Run it once on a
-- fresh database, or to rebuild from scratch (it will wipe existing data).
-- ============================================================================

-- Create the database (run only once; ignore "already exists" error)
CREATE DATABASE expense_tracker;
\connect expense_tracker;

DROP VIEW IF EXISTS v_monthly_summary;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS categories;

-- ============================================================================
-- users
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,          -- BCrypt hash, never plain text
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- categories  (owned by a user; unique per user: name + type)
-- ============================================================================
CREATE TABLE IF NOT EXISTS categories (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name    VARCHAR(50)       NOT NULL,
    type    VARCHAR(255)      NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    color   VARCHAR(7)        NOT NULL DEFAULT '#64748B',
    UNIQUE (user_id, name, type)
);
CREATE INDEX IF NOT EXISTS idx_categories_user_id ON categories(user_id);

-- ============================================================================
-- transactions  (money in/out; amount is NUMERIC, never float)
-- ============================================================================
CREATE TABLE IF NOT EXISTS transactions (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT           NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id      BIGINT           NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    type             VARCHAR(255)     NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    amount           NUMERIC(14, 2)   NOT NULL CHECK (amount > 0),
    description      VARCHAR(200)     NOT NULL,
    transaction_date DATE             NOT NULL,
    created_at       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_category_id ON transactions(category_id);

-- ============================================================================
-- automatic updated_at for transactions
-- ============================================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_transactions_updated_at ON transactions;
CREATE TRIGGER trg_transactions_updated_at
    BEFORE UPDATE ON transactions
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- ============================================================================
-- Helpful monthly summary view
-- ============================================================================
CREATE OR REPLACE VIEW v_monthly_summary AS
SELECT
    t.user_id,
    DATE_TRUNC('month', t.transaction_date) AS month,
    t.type,
    COUNT(*)      AS transaction_count,
    SUM(t.amount) AS total_amount
FROM transactions t
GROUP BY t.user_id, DATE_TRUNC('month', t.transaction_date), t.type;