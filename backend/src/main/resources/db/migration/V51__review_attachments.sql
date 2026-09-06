CREATE TABLE aso_review_attachment (
    id VARCHAR(36) PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content LONGBLOB NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE aso_order_review_attachment (
    order_id BIGINT NOT NULL,
    attachment_id VARCHAR(36) NOT NULL,
    region_code VARCHAR(16) NOT NULL,
    PRIMARY KEY (order_id, attachment_id),
    FOREIGN KEY (attachment_id) REFERENCES aso_review_attachment(id)
);
