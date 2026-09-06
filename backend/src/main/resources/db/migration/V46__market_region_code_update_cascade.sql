ALTER TABLE customer_app_region
    DROP FOREIGN KEY fk_customer_app_region_region;

ALTER TABLE customer_app_region
    ADD CONSTRAINT fk_customer_app_region_region
        FOREIGN KEY (region_code) REFERENCES market_region(code)
        ON UPDATE CASCADE;
