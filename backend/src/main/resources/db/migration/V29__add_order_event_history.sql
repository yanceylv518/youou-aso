CREATE TABLE aso_order_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    quantity_before INT NULL,
    quantity_after INT NULL,
    amount_before DECIMAL(18, 2) NULL,
    amount_after DECIMAL(18, 2) NULL,
    created_by_admin_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_event_order (order_id, created_at, id),
    CONSTRAINT fk_order_event_order FOREIGN KEY (order_id) REFERENCES aso_order(id)
);