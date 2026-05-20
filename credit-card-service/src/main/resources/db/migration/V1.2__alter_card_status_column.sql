ALTER TABLE credit_card_db.credit_cards 
ALTER COLUMN card_status TYPE VARCHAR(20) USING card_status::text;
