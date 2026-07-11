CREATE TABLE customer_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    force_password_change TINYINT NOT NULL DEFAULT 0,
    preferred_locale VARCHAR(16) NOT NULL DEFAULT 'zh-CN',
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE admin_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_code VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    force_password_change TINYINT NOT NULL DEFAULT 0,
    preferred_locale VARCHAR(16) NOT NULL DEFAULT 'zh-CN',
    last_login_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE wallet_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    frozen_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE aso_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    customer_app_id BIGINT NOT NULL,
    source_audit_id BIGINT NULL,
    order_type VARCHAR(32) NOT NULL,
    store_type VARCHAR(32) NOT NULL,
    app_identifier VARCHAR(255) NOT NULL,
    app_name VARCHAR(255) NOT NULL,
    app_icon_url VARCHAR(500) NULL,
    status VARCHAR(32) NOT NULL,
    order_start_date DATE NULL,
    order_end_date DATE NULL,
    execution_hours INT NULL,
    total_days INT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    balance_before DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    deducted_transaction_id BIGINT NULL,
    refund_transaction_id BIGINT NULL,
    reject_reason VARCHAR(500) NULL,
    confirmed_by_admin_id BIGINT NULL,
    confirmed_at DATETIME NULL,
    executed_by_admin_id BIGINT NULL,
    executed_at DATETIME NULL,
    expected_completed_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_status (customer_id, status),
    INDEX idx_store_status (store_type, status),
    INDEX idx_expected_completed (status, expected_completed_at)
);
