ALTER TABLE pricing_config
    ADD COLUMN china_unit_price DECIMAL(18, 4) NOT NULL DEFAULT 0.0000 AFTER unit_price;

UPDATE pricing_config
SET china_unit_price = unit_price;