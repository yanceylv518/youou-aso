CREATE TABLE order_module_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module_name VARCHAR(100) NOT NULL,
    order_type VARCHAR(64) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    china_unit_price DECIMAL(18,4) NOT NULL DEFAULT 0.0000,
    enabled TINYINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_order_module_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_module_china_price CHECK (china_unit_price >= 0),
    INDEX idx_order_module_type_enabled (order_type, enabled, sort_order)
);

INSERT INTO order_module_config (module_name, order_type, unit_price, china_unit_price, enabled, sort_order)
SELECT CASE code
           WHEN 'KEYWORD_INSTALL' THEN '关键词安装'
           WHEN 'DOWNLOAD' THEN '下载量'
           WHEN 'RATING_5' THEN '星级评分'
           WHEN 'REVIEW_5' THEN '用户评价'
       END,
       CASE code
           WHEN 'RATING_5' THEN 'RATING'
           WHEN 'REVIEW_5' THEN 'REVIEW'
           ELSE code
       END,
       unit_price,
       china_unit_price,
       enabled,
       sort_order
FROM pricing_config
WHERE code IN ('KEYWORD_INSTALL', 'DOWNLOAD', 'RATING_5', 'REVIEW_5');

ALTER TABLE aso_order
    ADD COLUMN order_module_id BIGINT NULL AFTER order_type,
    ADD COLUMN order_module_name VARCHAR(100) NULL AFTER order_module_id;
