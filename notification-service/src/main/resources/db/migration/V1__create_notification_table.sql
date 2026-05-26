CREATE SCHEMA IF NOT EXISTS notification_db;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE source_service AS ENUM ('ACCOUNT_SERVICE', 'LOAN_SERVICE', 'CREDIT_CARD_SERVICE', 'TRANSACTION_SERVICE', 'BANK_SERVICE');

CREATE TYPE notification_type AS ENUM (

    -- Account Service Events
    'ACCOUNT_CREATED',
    'DEPOSIT',
    'WITHDRAWAL',
    'TRANSFER',

    -- Loan Service Events
    'LOAN_APPLIED',
    'LOAN_APPROVED',
    'LOAN_REJECTED',
    'LOAN_DISBURSED',

    -- Credit Card Service Events
    'CREDIT_CARD_APPLIED',
    'CREDIT_CARD_APPROVED',
    'CREDIT_CARD_REJECTED',
    'CREDIT_CARD_ACTIVATED',
    'CREDIT_CARD_BLOCKED',
    'CREDIT_CARD_UNBLOCKED',
    'CREDIT_CARD_CLOSED',
    'CREDIT_CARD_CHARGE',
    'CREDIT_CARD_PAYMENT',

    -- Bank Service Events
    'BANK_REGISTERED'
);

CREATE TYPE channel_type AS ENUM ('EMAIL', 'SMS', 'PUSH_NOTIFICATION');

CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'FAILED', 'RETRYING');

CREATE TABLE notification_db.notification (

        notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

        customer_id UUID NOT NULL,

        source_service source_service NOT NULL,

        notification_type notification_type NOT NULL,

        channel_type channel_type NOT NULL,

        reference_id VARCHAR(255) NOT NULL ,

        message VARCHAR(255) NOT NULL ,

        subject VARCHAR(255) NOT NULL ,

        metadata VARCHAR(255) NOT NULL ,

        status notification_status NOT NULL,

        retry_count INT NOT NULL DEFAULT 0,

        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

        sent_at TIMESTAMP
);