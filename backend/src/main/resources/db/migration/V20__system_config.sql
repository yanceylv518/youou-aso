CREATE TABLE system_config (
    config_key VARCHAR(120) PRIMARY KEY,
    config_value TEXT,
    secret TINYINT NOT NULL DEFAULT 0,
    description VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO system_config (config_key, config_value, secret, description)
VALUES
    ('mail.smtp.host', NULL, 0, 'SMTP server host'),
    ('mail.smtp.port', '587', 0, 'SMTP server port'),
    ('mail.smtp.username', NULL, 0, 'SMTP username'),
    ('mail.smtp.password', NULL, 1, 'SMTP password or app password'),
    ('mail.from', NULL, 0, 'Default sender email address'),
    ('mail.smtp.auth', 'true', 0, 'Enable SMTP authentication'),
    ('mail.smtp.starttls.enable', 'false', 0, 'Enable SMTP STARTTLS'),
    ('mail.smtp.ssl.enable', 'true', 0, 'Enable SMTP SSL'),
    ('order.notification.enabled', 'false', 0, 'Enable order success email notifications'),
    ('order.notification.recipients', NULL, 0, 'Order success notification recipients')
ON DUPLICATE KEY UPDATE config_key = VALUES(config_key);
