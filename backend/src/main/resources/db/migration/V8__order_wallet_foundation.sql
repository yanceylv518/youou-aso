ALTER TABLE aso_order
    ADD COLUMN pricing_code VARCHAR(64) NULL AFTER order_type,
    ADD COLUMN region_code VARCHAR(16) NULL AFTER store_type,
    ADD COLUMN quantity INT NULL AFTER total_days,
    ADD COLUMN unit_price DECIMAL(18,4) NULL AFTER quantity;

CREATE TABLE aso_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    item_type VARCHAR(64) NOT NULL,
    item_name VARCHAR(255) NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    metadata_json TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_aso_order_item_order (order_id)
);

CREATE TABLE special_order_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    audit_no VARCHAR(64) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    customer_app_id BIGINT NOT NULL,
    order_type VARCHAR(32) NOT NULL,
    store_type VARCHAR(32) NOT NULL,
    region_code VARCHAR(16) NOT NULL,
    app_identifier VARCHAR(255) NOT NULL,
    app_name VARCHAR(255) NOT NULL,
    app_icon_url VARCHAR(500) NULL,
    requested_content TEXT NOT NULL,
    negotiated_content TEXT NULL,
    negotiated_price DECIMAL(18,2) NULL,
    status VARCHAR(32) NOT NULL,
    reviewed_by_admin_id BIGINT NULL,
    reviewed_at DATETIME NULL,
    cancel_reason VARCHAR(500) NULL,
    submitted_order_id BIGINT NULL,
    submitted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_special_audit_customer_status (customer_id, status),
    INDEX idx_special_audit_status (status)
);

CREATE TABLE wallet_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_no VARCHAR(64) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL,
    direction VARCHAR(16) NOT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    balance_before DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    related_order_id BIGINT NULL,
    remark VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_wallet_transaction_customer_time (customer_id, created_at),
    INDEX idx_wallet_transaction_order (related_order_id)
);
