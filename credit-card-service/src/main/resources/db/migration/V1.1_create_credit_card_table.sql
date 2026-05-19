CREATE SCHEMA IF NOT EXISTS credit_card_db;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE credit_card_db.credit_card(

    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    card_number VARCHAR(30) UNIQUE NOT NULL,

    customer_id UUID NOT NULL,

    account_number VARCHAR(20) NOT NULL ,

    card_holder_name VARCHAR(255) NOT NULL ,

    available_limit NUMERIC(15,2) NOT NULL ,

    outstanding_balance NUMERIC(15,2) NOT NULL ,

    credit_limit NUMERIC(15,2) NOT NULL ,

    minimum_due_amount NUMERIC(15,2) ,

    annual_limit NUMERIC(10,2) ,

    interest_rate DECIMAL,

    card_status credit_card_db.card_status NOT NULL ,

    expiry_date DATE NOT NULL,

    billing_cycle_day INT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,






)