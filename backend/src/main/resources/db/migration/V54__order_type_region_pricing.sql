CREATE TABLE order_type_region_config (
    order_type VARCHAR(64) NOT NULL,
    region_code VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_type, region_code),
    CONSTRAINT fk_order_type_region_config_region FOREIGN KEY (region_code) REFERENCES market_region(code) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE pricing_region_override (
    price_code VARCHAR(64) NOT NULL,
    region_code VARCHAR(16) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (price_code, region_code),
    CONSTRAINT fk_pricing_region_override_region FOREIGN KEY (region_code) REFERENCES market_region(code) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_pricing_region_override_non_negative CHECK (unit_price >= 0)
);

INSERT INTO order_type_region_config (order_type, region_code)
SELECT order_type, code
FROM market_region
CROSS JOIN (
    SELECT 'KEYWORD_INSTALL' AS order_type UNION ALL
    SELECT 'DOWNLOAD' UNION ALL
    SELECT 'RATING' UNION ALL
    SELECT 'REVIEW' UNION ALL
    SELECT 'RANK_GUARANTEE' UNION ALL
    SELECT 'CHART_RANK_GUARANTEE' UNION ALL
    SELECT 'KEYWORD_COVERAGE'
) order_types
WHERE enabled = 1
  AND NOT (order_type IN ('RATING', 'REVIEW') AND code = 'CN');
