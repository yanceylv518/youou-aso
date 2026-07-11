CREATE TABLE customer_app_region (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_app_id BIGINT NOT NULL,
    region_code VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customer_app_region (customer_app_id, region_code),
    INDEX idx_customer_app_region_code (region_code),
    CONSTRAINT fk_customer_app_region_app FOREIGN KEY (customer_app_id) REFERENCES customer_app(id),
    CONSTRAINT fk_customer_app_region_region FOREIGN KEY (region_code) REFERENCES market_region(code)
);

INSERT IGNORE INTO customer_app_region (customer_app_id, region_code)
SELECT canonical.keep_id, app.region_code
FROM customer_app app
JOIN (
    SELECT MIN(id) AS keep_id, customer_id, store_type, app_identifier
    FROM customer_app
    WHERE status = 'ACTIVE'
    GROUP BY customer_id, store_type, app_identifier
) canonical
    ON canonical.customer_id = app.customer_id
    AND canonical.store_type = app.store_type
    AND canonical.app_identifier = app.app_identifier
WHERE app.status = 'ACTIVE';

UPDATE customer_app app
JOIN (
    SELECT MIN(id) AS keep_id, customer_id, store_type, app_identifier
    FROM customer_app
    WHERE status = 'ACTIVE'
    GROUP BY customer_id, store_type, app_identifier
    HAVING COUNT(*) > 1
) canonical
    ON canonical.customer_id = app.customer_id
    AND canonical.store_type = app.store_type
    AND canonical.app_identifier = app.app_identifier
SET app.status = 'DISABLED',
    app.updated_at = CURRENT_TIMESTAMP
WHERE app.status = 'ACTIVE'
  AND app.id <> canonical.keep_id;

ALTER TABLE customer_app
    DROP INDEX uk_customer_app_active;

ALTER TABLE customer_app
    ADD UNIQUE KEY uk_customer_app_active (
        customer_id,
        store_type,
        active_app_identifier
    );
