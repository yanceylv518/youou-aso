CREATE TABLE pricing_config (
    code VARCHAR(64) PRIMARY KEY,
    unit_price DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    enabled TINYINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_pricing_config_unit_price_non_negative CHECK (unit_price >= 0)
);

INSERT INTO pricing_config (code, unit_price, enabled, sort_order)
VALUES
    ('KEYWORD_INSTALL', 0.0000, 1, 10),
    ('DOWNLOAD', 0.0000, 1, 20),
    ('RATING_5', 0.0000, 1, 30),
    ('RATING_4', 0.0000, 1, 40),
    ('REVIEW_5', 0.0000, 1, 50),
    ('REVIEW_4', 0.0000, 1, 60);
