ALTER TABLE customer_app
    ADD COLUMN category VARCHAR(128) NULL AFTER external_app_id;
