ALTER TABLE notification_db.notification 
ALTER COLUMN source_service TYPE VARCHAR(50) USING source_service::text,
ALTER COLUMN notification_type TYPE VARCHAR(50) USING notification_type::text,
ALTER COLUMN channel_type TYPE VARCHAR(30) USING channel_type::text,
ALTER COLUMN status TYPE VARCHAR(20) USING status::text;
