CREATE SCHEMA IF NOT EXISTS account_db;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE account_db.account_type AS ENUM (
    'SAVINGS',
    'CURRENT',
    'FIXED_DEPOSIT'
);

CREATE TYPE account_db.account_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'BLOCKED',
    'CLOSED'
);

CREATE TABLE account_db.account (
        account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

        customer_id UUID NOT NULL,
        bank_id UUID NOT NULL,

        account_number VARCHAR(255) UNIQUE NOT NULL,

        account_type account_db.account_type NOT NULL,

        status account_db.account_status NOT NULL,

        balance DECIMAL(15, 2) NOT NULL,

        ifsc_code VARCHAR(255) NOT NULL,

        branch_name VARCHAR(255) NOT NULL,

        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE account_db.idempotency_request (

    id BIGSERIAL PRIMARY KEY,

    idempotency_key VARCHAR(255) UNIQUE NOT NULL,

    account_number VARCHAR(255) NOT NULL,

    status VARCHAR(255) NOT NULL
);