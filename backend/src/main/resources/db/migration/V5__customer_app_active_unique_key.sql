ALTER TABLE customer_app
    DROP INDEX uk_customer_app;

ALTER TABLE customer_app
    ADD COLUMN active_app_identifier VARCHAR(255)
        GENERATED ALWAYS AS (
            CASE
                WHEN status = 'ACTIVE' THEN app_identifier
                ELSE NULL
            END
        ) STORED AFTER app_identifier;

ALTER TABLE customer_app
    ADD UNIQUE KEY uk_customer_app_active (
        customer_id,
        store_type,
        region_code,
        active_app_identifier
    );
