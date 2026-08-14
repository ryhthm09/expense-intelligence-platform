-- ============================================================
-- V1: Initial schema for Expense Intelligence Platform
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ------------------------------------------------------------
-- Users
-- ------------------------------------------------------------
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users (email);

-- ------------------------------------------------------------
-- Categories
-- ------------------------------------------------------------
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users (id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(20) NOT NULL,
    icon        VARCHAR(50),
    color       VARCHAR(7),
    is_system   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_categories_type CHECK (type IN ('EXPENSE', 'INCOME')),
    CONSTRAINT uq_categories_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_categories_user_id ON categories (user_id);

-- ------------------------------------------------------------
-- Merchants
-- ------------------------------------------------------------
CREATE TABLE merchants (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID REFERENCES users (id) ON DELETE CASCADE,
    name             VARCHAR(255) NOT NULL,
    normalized_name  VARCHAR(255) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_merchants_user_normalized UNIQUE (user_id, normalized_name)
);

CREATE INDEX idx_merchants_user_id ON merchants (user_id);
CREATE INDEX idx_merchants_normalized_name ON merchants (normalized_name);

-- ------------------------------------------------------------
-- Transactions
-- ------------------------------------------------------------
CREATE TABLE transactions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id      UUID REFERENCES categories (id) ON DELETE SET NULL,
    merchant_id      UUID REFERENCES merchants (id) ON DELETE SET NULL,
    amount           NUMERIC(19, 4) NOT NULL,
    currency         VARCHAR(3) NOT NULL DEFAULT 'USD',
    description      VARCHAR(500),
    transaction_date DATE NOT NULL,
    type             VARCHAR(20) NOT NULL,
    source           VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    external_id      VARCHAR(255),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_transactions_type CHECK (type IN ('DEBIT', 'CREDIT', 'TRANSFER')),
    CONSTRAINT chk_transactions_source CHECK (source IN ('MANUAL', 'CSV_IMPORT', 'API')),
    CONSTRAINT uq_transactions_user_external UNIQUE (user_id, external_id)
);

CREATE INDEX idx_transactions_user_id ON transactions (user_id);
CREATE INDEX idx_transactions_date ON transactions (transaction_date);
CREATE INDEX idx_transactions_category_id ON transactions (category_id);
CREATE INDEX idx_transactions_merchant_id ON transactions (merchant_id);

-- ------------------------------------------------------------
-- Budgets
-- ------------------------------------------------------------
CREATE TABLE budgets (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories (id) ON DELETE SET NULL,
    name        VARCHAR(100) NOT NULL,
    amount      NUMERIC(19, 4) NOT NULL,
    period      VARCHAR(20) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_budgets_period CHECK (period IN ('WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY', 'CUSTOM')),
    CONSTRAINT chk_budgets_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_budgets_user_id ON budgets (user_id);
CREATE INDEX idx_budgets_category_id ON budgets (category_id);

-- ------------------------------------------------------------
-- Seed system categories (available to all users)
-- ------------------------------------------------------------
INSERT INTO categories (id, user_id, name, type, icon, color, is_system) VALUES
    (gen_random_uuid(), NULL, 'Food & Dining',       'EXPENSE', 'utensils',    '#EF4444', TRUE),
    (gen_random_uuid(), NULL, 'Transportation',      'EXPENSE', 'car',         '#F97316', TRUE),
    (gen_random_uuid(), NULL, 'Shopping',            'EXPENSE', 'shopping-bag','#EAB308', TRUE),
    (gen_random_uuid(), NULL, 'Entertainment',       'EXPENSE', 'film',        '#8B5CF6', TRUE),
    (gen_random_uuid(), NULL, 'Bills & Utilities',   'EXPENSE', 'receipt',     '#3B82F6', TRUE),
    (gen_random_uuid(), NULL, 'Healthcare',          'EXPENSE', 'heart-pulse', '#EC4899', TRUE),
    (gen_random_uuid(), NULL, 'Travel',              'EXPENSE', 'plane',       '#06B6D4', TRUE),
    (gen_random_uuid(), NULL, 'Education',           'EXPENSE', 'graduation-cap','#10B981', TRUE),
    (gen_random_uuid(), NULL, 'Salary',              'INCOME',  'briefcase',   '#22C55E', TRUE),
    (gen_random_uuid(), NULL, 'Investments',         'INCOME',  'trending-up', '#14B8A6', TRUE),
    (gen_random_uuid(), NULL, 'Other Income',        'INCOME',  'plus-circle', '#64748B', TRUE),
    (gen_random_uuid(), NULL, 'Other Expense',       'EXPENSE', 'more-horizontal','#94A3B8', TRUE);
