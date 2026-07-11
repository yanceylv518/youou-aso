ALTER TABLE aso_order_item
    ADD COLUMN region_code VARCHAR(16) NULL AFTER item_name,
    ADD INDEX idx_aso_order_item_region (region_code);
