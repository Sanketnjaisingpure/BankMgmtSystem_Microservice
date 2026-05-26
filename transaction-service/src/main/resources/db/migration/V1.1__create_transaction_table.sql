CREATE SCHEMA IF NOT EXISTS transaction_db;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE transaction_db.transaction_type AS ENUM (
    'DEPOSIT',
    'WITHDRAW',
    'TRANSFER'
);

CREATE TYPE transaction_db.transaction_status AS ENUM (
    'PENDING',
    'SUCCESS',
    'FAILED'
);

CREATE TABLE transaction_db.transactions(
    transaction_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    transaction_type transaction_db.transaction_type NOT NULL,
    transaction_status transaction_db.transaction_status NOT NULL,
    amount DECIMAL(15,2) NOT  NULL ,
    source_account_number VARCHAR(255) NOT NULL,
    destination_account_number VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_description VARCHAR(255) NOT NULL
)