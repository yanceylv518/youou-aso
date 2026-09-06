ALTER TABLE customer_service_config
    ADD COLUMN email_visible TINYINT NOT NULL DEFAULT 0 AFTER email,
    ADD COLUMN phone VARCHAR(40) NULL AFTER email_visible,
    ADD COLUMN phone_visible TINYINT NOT NULL DEFAULT 0 AFTER phone,
    ADD COLUMN telegram_qr_url VARCHAR(1024) NULL AFTER telegram_url,
    ADD COLUMN telegram_qr_visible TINYINT NOT NULL DEFAULT 0 AFTER telegram_qr_url,
    ADD COLUMN wechat_qr_url VARCHAR(1024) NULL AFTER telegram_qr_visible,
    ADD COLUMN wechat_qr_visible TINYINT NOT NULL DEFAULT 0 AFTER wechat_qr_url;

UPDATE customer_service_config
SET email_visible = CASE WHEN email IS NOT NULL AND email <> '' THEN 1 ELSE 0 END;
