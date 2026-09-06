ALTER TABLE customer_service_config
    ADD COLUMN email VARCHAR(254) NULL AFTER contact_hint,
    ADD COLUMN teams_url VARCHAR(1024) NULL AFTER email,
    ADD COLUMN telegram_url VARCHAR(1024) NULL AFTER teams_url,
    ADD COLUMN whatsapp_url VARCHAR(1024) NULL AFTER telegram_url;
