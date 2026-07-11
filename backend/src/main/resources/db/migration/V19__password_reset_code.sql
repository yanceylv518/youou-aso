CREATE TABLE password_reset_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(128) NOT NULL,
    account_type VARCHAR(32) NOT NULL,
    account_id BIGINT NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_password_reset_email_active (email, used_at, expires_at),
    INDEX idx_password_reset_account (account_type, account_id)
);
