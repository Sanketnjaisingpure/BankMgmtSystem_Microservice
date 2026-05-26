ALTER TABLE loan_db.loan 
ALTER COLUMN loan_status TYPE VARCHAR(20) USING loan_status::text;
