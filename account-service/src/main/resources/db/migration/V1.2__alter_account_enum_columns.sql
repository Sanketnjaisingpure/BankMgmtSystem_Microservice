ALTER TABLE account_db.account 
ALTER COLUMN account_type TYPE VARCHAR(30) USING account_type::text,
ALTER COLUMN status TYPE VARCHAR(20) USING status::text;
