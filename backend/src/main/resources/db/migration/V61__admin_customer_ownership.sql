ALTER TABLE customer_account ADD COLUMN owner_admin_id BIGINT NULL,
    ADD INDEX idx_customer_owner (owner_admin_id, id),
    ADD CONSTRAINT fk_customer_owner FOREIGN KEY (owner_admin_id) REFERENCES admin_account(id);
CREATE TABLE admin_referral (
    admin_id BIGINT PRIMARY KEY,
    invite_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_referral_admin FOREIGN KEY (admin_id) REFERENCES admin_account(id)
);
CREATE TABLE admin_customer_service_config LIKE customer_service_config;
ALTER TABLE admin_customer_service_config ADD CONSTRAINT fk_support_admin FOREIGN KEY (id) REFERENCES admin_account(id);
