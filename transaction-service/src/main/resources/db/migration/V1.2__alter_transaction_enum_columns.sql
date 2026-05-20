ALTER TABLE transaction_db.transactions 
ALTER COLUMN transaction_type TYPE VARCHAR(20) USING transaction_type::text,
ALTER COLUMN transaction_status TYPE VARCHAR(20) USING transaction_status::text;
