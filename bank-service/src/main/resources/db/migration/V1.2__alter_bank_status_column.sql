ALTER TABLE bank_db.bank 
ALTER COLUMN bank_status TYPE VARCHAR(20) USING bank_status::text;
