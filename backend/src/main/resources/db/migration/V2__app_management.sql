CREATE TABLE market_region (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(16) NOT NULL UNIQUE,
    name_zh VARCHAR(64) NOT NULL,
    name_en VARCHAR(64) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO market_region (code, name_zh, name_en, enabled, sort_order) VALUES
('US', '美国', 'United States', 1, 10),
('CN', '中国大陆', 'Mainland China', 1, 20),
('HK', '中国香港', 'Hong Kong', 1, 30),
('TW', '中国台湾', 'Taiwan', 1, 40),
('JP', '日本', 'Japan', 1, 50),
('KR', '韩国', 'South Korea', 1, 60),
('SG', '新加坡', 'Singapore', 1, 70),
('GB', '英国', 'United Kingdom', 1, 80),
('CA', '加拿大', 'Canada', 1, 90),
('AU', '澳大利亚', 'Australia', 1, 100);

CREATE TABLE customer_app (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    store_type VARCHAR(32) NOT NULL,
    region_code VARCHAR(16) NOT NULL,
    app_identifier VARCHAR(255) NOT NULL,
    app_name VARCHAR(255) NOT NULL,
    app_icon_url VARCHAR(500) NULL,
    bundle_id VARCHAR(255) NULL,
    external_app_id VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    verified_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customer_app (customer_id, store_type, region_code, app_identifier),
    INDEX idx_customer_store (customer_id, store_type),
    INDEX idx_store_region_identifier (store_type, region_code, app_identifier),
    CONSTRAINT fk_customer_app_customer FOREIGN KEY (customer_id) REFERENCES customer_account(id)
);
