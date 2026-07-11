CREATE TABLE customer_service_config (
    id BIGINT PRIMARY KEY,
    service_name VARCHAR(120) NOT NULL,
    qr_code_url VARCHAR(1024),
    contact_hint VARCHAR(512),
    enabled TINYINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO customer_service_config (id, service_name, qr_code_url, contact_hint, enabled)
VALUES (1, 'Youou-ASO Support', NULL, 'Scan the QR code to contact customer service for recharge.', 0);
