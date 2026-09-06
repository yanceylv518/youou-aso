ALTER TABLE order_module_config
    MODIFY COLUMN unit_price DECIMAL(18,4) NULL,
    MODIFY COLUMN china_unit_price DECIMAL(18,4) NULL;

UPDATE order_module_config
SET unit_price = NULL,
    china_unit_price = NULL
WHERE order_type IN ('RANK_GUARANTEE', 'CHART_RANK_GUARANTEE', 'KEYWORD_COVERAGE');

INSERT INTO order_module_config (module_name, order_type, unit_price, china_unit_price, enabled, sort_order)
SELECT '关键词保排名', 'RANK_GUARANTEE', NULL, NULL, 1, 50
WHERE NOT EXISTS (SELECT 1 FROM order_module_config WHERE order_type = 'RANK_GUARANTEE');

INSERT INTO order_module_config (module_name, order_type, unit_price, china_unit_price, enabled, sort_order)
SELECT '榜单保排名', 'CHART_RANK_GUARANTEE', NULL, NULL, 1, 60
WHERE NOT EXISTS (SELECT 1 FROM order_module_config WHERE order_type = 'CHART_RANK_GUARANTEE');

INSERT INTO order_module_config (module_name, order_type, unit_price, china_unit_price, enabled, sort_order)
SELECT '关键词覆盖', 'KEYWORD_COVERAGE', NULL, NULL, 1, 70
WHERE NOT EXISTS (SELECT 1 FROM order_module_config WHERE order_type = 'KEYWORD_COVERAGE');
