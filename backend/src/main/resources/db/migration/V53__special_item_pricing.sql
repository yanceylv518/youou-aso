ALTER TABLE special_order_audit_item
    ADD COLUMN unit_price DECIMAL(12, 2) NULL AFTER coverage_note,
    ADD COLUMN execution_days INT NULL AFTER unit_price;
