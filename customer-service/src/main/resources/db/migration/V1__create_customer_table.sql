CREATE SCHEMA IF NOT EXISTS customer_db;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE customer_db.customer (

      customer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

      first_name VARCHAR(255) NOT NULL,

      last_name VARCHAR(255) NOT NULL,

      email VARCHAR(255) NOT NULL UNIQUE,

      phone_number VARCHAR(255) NOT NULL UNIQUE,

      password_hash VARCHAR(255) NOT NULL,

      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);