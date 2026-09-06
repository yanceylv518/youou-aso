INSERT INTO system_config (config_key, config_value, secret, description)
VALUES
    ('home.metrics.apps.value', '10,000+', 0, 'Public home served apps metric'),
    ('home.metrics.satisfaction.value', '98.6%', 0, 'Public home satisfaction metric'),
    ('home.metrics.experience.years', '5', 0, 'Public home experience years'),
    ('home.metrics.team.value', '50+', 0, 'Public home team metric')
ON DUPLICATE KEY UPDATE config_key = VALUES(config_key);

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'MENU', 'system.homeMetrics', '首页数据', 'Home Metrics', '/admin/home-metrics', 'homeMetrics:view', 'DataAnalysis', 5, TRUE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'system'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_code = 'system.homeMetrics');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'homeMetrics.update', '修改首页数据', 'Update home metrics', NULL, 'homeMetrics:update', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'system.homeMetrics'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'homeMetrics:update');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.menu_code IN ('system.homeMetrics', 'homeMetrics.update')
WHERE r.role_key = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id);
