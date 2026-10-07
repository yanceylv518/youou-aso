ALTER TABLE order_module_config ADD COLUMN store_types VARCHAR(100) NOT NULL DEFAULT 'APP_STORE,GOOGLE_PLAY,IPAD_STORE';
CREATE TABLE order_module_region_config (
 module_id BIGINT NOT NULL, region_code VARCHAR(16) NOT NULL,
 PRIMARY KEY(module_id,region_code),
 FOREIGN KEY(module_id) REFERENCES order_module_config(id) ON DELETE CASCADE,
 FOREIGN KEY(region_code) REFERENCES market_region(code) ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE order_module_region_price (
 module_id BIGINT NOT NULL, price_code VARCHAR(64) NOT NULL, region_code VARCHAR(16) NOT NULL, unit_price DECIMAL(18,4) NOT NULL,
 PRIMARY KEY(module_id,price_code,region_code),
 FOREIGN KEY(module_id) REFERENCES order_module_config(id) ON DELETE CASCADE,
 FOREIGN KEY(region_code) REFERENCES market_region(code) ON UPDATE CASCADE ON DELETE CASCADE,
 CHECK(unit_price>=0)
);
INSERT INTO order_module_region_config(module_id,region_code)
 SELECT m.id,r.region_code FROM order_module_config m JOIN order_type_region_config r ON r.order_type=m.order_type;
INSERT INTO order_module_region_price(module_id,price_code,region_code,unit_price)
 SELECT m.id,p.price_code,p.region_code,p.unit_price FROM order_module_config m JOIN pricing_region_override p
 ON p.price_code=m.order_type OR (m.order_type='RATING' AND p.price_code IN ('RATING_4','RATING_5')) OR (m.order_type='REVIEW' AND p.price_code IN ('REVIEW_4','REVIEW_5'));
ALTER TABLE special_order_audit ADD COLUMN order_module_id BIGINT NULL;
