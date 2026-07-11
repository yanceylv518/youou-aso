CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_key VARCHAR(64) NOT NULL UNIQUE,
    role_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    sort_order INT NOT NULL DEFAULT 0,
    built_in TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NULL,
    menu_type VARCHAR(32) NOT NULL,
    menu_code VARCHAR(128) NOT NULL UNIQUE,
    name_zh VARCHAR(128) NOT NULL,
    name_en VARCHAR(128) NOT NULL,
    route_path VARCHAR(255) NULL,
    permission_code VARCHAR(128) NULL,
    icon VARCHAR(64) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sys_menu_parent (parent_id),
    INDEX idx_sys_menu_permission (permission_code)
);

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
);

CREATE TABLE admin_account_role (
    admin_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (admin_id, role_id),
    CONSTRAINT fk_admin_account_role_admin FOREIGN KEY (admin_id) REFERENCES admin_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_account_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);

INSERT INTO sys_role (role_key, role_name, status, sort_order, built_in, remark) VALUES
('SUPER_ADMIN', '超级管理员', 'ENABLED', 1, 1, '系统内置，拥有全部权限'),
('OPERATOR', '运营管理员', 'ENABLED', 10, 1, '应用、订单和执行管理'),
('FINANCE', '财务管理员', 'ENABLED', 20, 1, '财务流水、充值和余额调整'),
('CONFIG_ADMIN', '配置管理员', 'ENABLED', 30, 1, '价格、地区、客服和邮件配置'),
('READONLY', '只读管理员', 'ENABLED', 40, 1, '仅查看主要列表和详情');

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible) VALUES
(NULL, 'MENU', 'dashboard', '首页', 'Home', '/admin/dashboard', 'dashboard:view', 'HomeFilled', 10, 1),
(NULL, 'MENU', 'customers', '用户管理', 'Customers', '/admin/customers', 'customer:list', 'User', 20, 1),
(NULL, 'MENU', 'promotion', '推广服务', 'Promotion Services', '/admin/promotion', 'promotion:view', 'Promotion', 30, 1),
(NULL, 'MENU', 'applications', '应用管理', 'Applications', '/admin/applications', 'app:list', 'Grid', 40, 1),
(NULL, 'DIRECTORY', 'order_execution', '订单执行', 'Order Execution', '/admin/order-execution', NULL, 'Finished', 50, 1),
(NULL, 'DIRECTORY', 'orders', '订单', 'Orders', '/admin/orders', NULL, 'Tickets', 60, 1),
(NULL, 'DIRECTORY', 'finance', '财务管理', 'Finance', '/admin/finance-group', NULL, 'Money', 70, 1),
(NULL, 'DIRECTORY', 'system', '系统管理', 'System', '/admin/system', NULL, 'Tools', 80, 1);

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'order_execution_pending', '待执行订单', 'Pending Execution', '/admin/orders/pending-execution', 'order:execute-list', 'Tickets', 10, 1 FROM sys_menu p WHERE p.menu_code = 'order_execution';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'order_execution_executing', '执行中订单', 'Executing Orders', '/admin/orders/executing', 'order:execute-list', 'Finished', 20, 1 FROM sys_menu p WHERE p.menu_code = 'order_execution';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'order_execution_completed', '已完成订单', 'Completed Orders', '/admin/orders/completed', 'order:list', 'DocumentChecked', 30, 1 FROM sys_menu p WHERE p.menu_code = 'order_execution';

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'orders_pending_review', '待审核订单', 'Pending Review', '/admin/pending-review-orders', 'special-audit:list', 'DocumentChecked', 10, 1 FROM sys_menu p WHERE p.menu_code = 'orders';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'orders_pending_confirm', '待确认订单', 'Pending Confirmation', '/admin/pending-confirm-orders', 'order:confirm-list', 'DocumentChecked', 20, 1 FROM sys_menu p WHERE p.menu_code = 'orders';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'orders_apple', '苹果订单', 'Apple Orders', '/admin/orders/apple', 'order:list', 'Apple', 30, 1 FROM sys_menu p WHERE p.menu_code = 'orders';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'orders_google', '谷歌订单', 'Google Orders', '/admin/orders/google', 'order:list', 'ChromeFilled', 40, 1 FROM sys_menu p WHERE p.menu_code = 'orders';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'orders_ipad', 'iPad 订单', 'iPad Orders', '/admin/orders/ipad', 'order:list', 'Monitor', 50, 1 FROM sys_menu p WHERE p.menu_code = 'orders';

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'finance_transactions', '财务流水', 'Financial Ledger', '/admin/finance', 'wallet:list', 'Notebook', 10, 1 FROM sys_menu p WHERE p.menu_code = 'finance';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'finance_recharges', '充值记录', 'Recharge Records', '/admin/finance/recharges', 'wallet:recharge', 'Money', 20, 1 FROM sys_menu p WHERE p.menu_code = 'finance';

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'system_pricing', '价格配置', 'Pricing', '/admin/pricing', 'system:pricing', 'PriceTag', 10, 1 FROM sys_menu p WHERE p.menu_code = 'system';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'system_wallet_types', '流水类型配置', 'Transaction Type Config', '/admin/wallet-transaction-types', 'system:wallet-type', 'Notebook', 20, 1 FROM sys_menu p WHERE p.menu_code = 'system';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'system_customer_service', '客服配置', 'Customer Service', '/admin/customer-service', 'system:customer-service', 'Service', 30, 1 FROM sys_menu p WHERE p.menu_code = 'system';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'system_mail', '邮件配置', 'Mail Config', '/admin/order-notification', 'system:mail', 'Message', 40, 1 FROM sys_menu p WHERE p.menu_code = 'system';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'system_regions', '地区配置', 'Regions', '/admin/regions', 'system:regions', 'Location', 50, 1 FROM sys_menu p WHERE p.menu_code = 'system';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'system_admin_accounts', '管理员账号', 'Admin Accounts', '/admin/admin-accounts', 'system:admin-account', 'Avatar', 60, 1 FROM sys_menu p WHERE p.menu_code = 'system';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'system_roles', '角色权限', 'Roles', '/admin/roles', 'system:role', 'Lock', 70, 1 FROM sys_menu p WHERE p.menu_code = 'system';
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT p.id, 'MENU', 'system_settings', '账户设置', 'Account Settings', '/admin/settings', 'account:settings', 'Setting', 80, 1 FROM sys_menu p WHERE p.menu_code = 'system';

INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT NULL, 'BUTTON', 'btn_app_add', '添加应用', 'Add app', NULL, 'app:add', NULL, 1000, 0;
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT NULL, 'BUTTON', 'btn_app_delete', '删除应用', 'Delete app', NULL, 'app:delete', NULL, 1001, 0;
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT NULL, 'BUTTON', 'btn_order_confirm', '确认订单', 'Confirm order', NULL, 'order:confirm', NULL, 1010, 0;
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT NULL, 'BUTTON', 'btn_order_execute', '执行订单', 'Execute order', NULL, 'order:execute', NULL, 1011, 0;
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT NULL, 'BUTTON', 'btn_order_cancel', '取消订单', 'Cancel order', NULL, 'order:cancel', NULL, 1012, 0;
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT NULL, 'BUTTON', 'btn_order_export', '导出订单', 'Export orders', NULL, 'order:export', NULL, 1013, 0;
INSERT INTO sys_menu (parent_id, menu_type, menu_code, name_zh, name_en, route_path, permission_code, icon, sort_order, visible)
SELECT NULL, 'BUTTON', 'btn_wallet_adjust', '余额调整', 'Adjust balance', NULL, 'wallet:adjust', NULL, 1020, 0;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id FROM sys_role r CROSS JOIN sys_menu m WHERE r.role_key = 'SUPER_ADMIN';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m ON m.menu_code IN (
    'dashboard','customers','promotion','applications','order_execution','order_execution_pending','order_execution_executing','order_execution_completed',
    'orders','orders_pending_review','orders_pending_confirm','orders_apple','orders_google','orders_ipad','system','system_settings',
    'btn_app_add','btn_app_delete','btn_order_confirm','btn_order_execute','btn_order_cancel','btn_order_export'
) WHERE r.role_key = 'OPERATOR';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m ON m.menu_code IN (
    'dashboard','finance','finance_transactions','finance_recharges','system','system_settings','btn_wallet_adjust'
) WHERE r.role_key = 'FINANCE';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m ON m.menu_code IN (
    'dashboard','system','system_pricing','system_wallet_types','system_customer_service','system_mail','system_regions','system_settings'
) WHERE r.role_key = 'CONFIG_ADMIN';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m ON m.menu_code IN (
    'dashboard','customers','applications','orders','orders_apple','orders_google','orders_ipad','finance','finance_transactions','system','system_settings'
) WHERE r.role_key = 'READONLY';

INSERT INTO admin_account_role (admin_id, role_id)
SELECT a.id, r.id
FROM admin_account a
JOIN sys_role r ON r.role_key = CASE WHEN a.role_code = 'SUPER_ADMIN' THEN 'SUPER_ADMIN' ELSE 'OPERATOR' END;