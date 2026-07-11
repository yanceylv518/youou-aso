ALTER TABLE aso_order
    ADD COLUMN contact_type VARCHAR(32) NULL AFTER app_icon_url,
    ADD COLUMN contact_value VARCHAR(128) NULL AFTER contact_type,
    ADD COLUMN remark VARCHAR(500) NULL AFTER contact_value;

CREATE TABLE aso_order_comment_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    region_code VARCHAR(16) NOT NULL,
    star_level INT NOT NULL,
    comment_title VARCHAR(120) NOT NULL,
    comment_content VARCHAR(1000) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_comment_detail_order (order_id),
    INDEX idx_order_comment_detail_region (region_code)
);

CREATE TABLE special_order_audit_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    audit_id BIGINT NOT NULL,
    region_code VARCHAR(16) NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    target_rank INT NULL,
    coverage_note VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_special_order_audit_item_audit (audit_id),
    INDEX idx_special_order_audit_item_region (region_code)
);
