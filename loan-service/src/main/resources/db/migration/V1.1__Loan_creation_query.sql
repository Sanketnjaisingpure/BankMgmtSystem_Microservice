CREATE SCHEMA IF NOT EXISTS loan_db;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE loan_db.loan_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'ACTIVE',
    'CLOSED'
);


CREATE TABLE loan_db.loan(

    loan_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    customer_id UUID NOT NULL ,

    account_number VARCHAR(255) NOT NULL,

    loan_amount NUMERIC(15,2) NOT NULL ,

    interest_rate DOUBLE PRECISION NOT NULL ,

    emi_amount NUMERIC(15,2) NOT NULL ,

    loan_status loan_db.loan_status NOT NULL,

    tenure_months INT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)