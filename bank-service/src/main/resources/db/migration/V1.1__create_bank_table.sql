CREATE SCHEMA IF NOT EXISTS bank_db;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE bank_db.bank_status AS ENUM (
    'ACTIVE',
    'SUSPENDED',
    'CLOSED'
);

CREATE TABLE bank_db.bank(
    bank_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bank_name VARCHAR(255) NOT NULL,
    bank_code VARCHAR(255) UNIQUE NOT NULL,
    headquarters_city VARCHAR(255) NOT NULL,
    ifsc_prefix VARCHAR(4) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(255) NOT NULL,
    bank_status bank_db.bank_status NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);