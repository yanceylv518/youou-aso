INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'customers.status', '启停用户', 'Enable or disable customers', NULL, 'customer:status', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'customers'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'customer:status');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'orders.createForCustomer', '代客下单', 'Create order for customer', NULL, 'order:create', NULL, 5, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'promotion'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'order:create');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'order.pause', '暂停订单', 'Pause orders', NULL, 'order:pause', NULL, 35, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'orderExecution.executing'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'order:pause');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'order.resume', '恢复订单', 'Resume orders', NULL, 'order:resume', NULL, 36, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'orderExecution.executing'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'order:resume');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'specialOrder.review', '审核特殊订单', 'Review special orders', NULL, 'specialOrder:review', NULL, 20, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'orders.pendingReview'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'specialOrder:review');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'specialOrder.cancel', '取消特殊订单', 'Cancel special orders', NULL, 'specialOrder:cancel', NULL, 30, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'orders.pendingReview'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'specialOrder:cancel');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'wallet.recharge', '账户充值', 'Recharge accounts', NULL, 'wallet:recharge', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'finance.recharges'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'wallet:recharge');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'walletTypes.update', '修改流水类型', 'Update transaction types', NULL, 'walletType:update', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'system.walletTypes'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'walletType:update');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'pricing.update', '修改价格', 'Update pricing', NULL, 'pricing:update', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'system.pricing'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'pricing:update');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'customerService.update', '修改客服配置', 'Update customer service config', NULL, 'customerService:update', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'system.customerService'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'customerService:update');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'mail.update', '修改邮件配置', 'Update mail config', NULL, 'mail:update', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'system.mail'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'mail:update');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'regions.update', '修改地区配置', 'Update regions', NULL, 'region:update', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'system.regions'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'region:update');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'admins.manage', '管理管理员', 'Manage admins', NULL, 'admin:manage', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'system.adminAccounts'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'admin:manage');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible, status)
SELECT p.id, 'BUTTON', 'roles.manage', '管理角色权限', 'Manage roles', NULL, 'role:manage', NULL, 10, FALSE, 'ENABLED'
FROM sys_menu p
WHERE p.menu_code = 'system.roles'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE permission_code = 'role:manage');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.permission_code IN (
    'customer:status', 'order:create', 'order:pause', 'order:resume',
    'specialOrder:review', 'specialOrder:cancel', 'wallet:recharge',
    'walletType:update', 'pricing:update', 'customerService:update', 'mail:update',
    'region:update', 'admin:manage', 'role:manage'
)
WHERE r.role_key = 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.permission_code IN (
    'customer:status', 'order:create', 'order:pause', 'order:resume',
    'specialOrder:review', 'specialOrder:cancel'
)
WHERE r.role_key = 'OPERATOR'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.permission_code IN ('wallet:recharge', 'wallet:adjust')
WHERE r.role_key = 'FINANCE'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.permission_code IN (
    'walletType:update', 'pricing:update', 'customerService:update', 'mail:update', 'region:update'
)
WHERE r.role_key = 'CONFIG_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id);