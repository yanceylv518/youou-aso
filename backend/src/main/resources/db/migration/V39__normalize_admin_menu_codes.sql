UPDATE sys_menu SET menu_code = 'orderExecution' WHERE menu_code = 'order_execution';
UPDATE sys_menu SET menu_code = 'orderExecution.pending' WHERE menu_code = 'order_execution_pending';
UPDATE sys_menu SET menu_code = 'orderExecution.executing' WHERE menu_code = 'order_execution_executing';
UPDATE sys_menu SET menu_code = 'orderExecution.completed' WHERE menu_code = 'order_execution_completed';

UPDATE sys_menu SET menu_code = 'orders.pendingReview' WHERE menu_code = 'orders_pending_review';
UPDATE sys_menu SET menu_code = 'orders.pendingConfirm' WHERE menu_code = 'orders_pending_confirm';
UPDATE sys_menu SET menu_code = 'orders.apple' WHERE menu_code = 'orders_apple';
UPDATE sys_menu SET menu_code = 'orders.google' WHERE menu_code = 'orders_google';
UPDATE sys_menu SET menu_code = 'orders.ipad' WHERE menu_code = 'orders_ipad';

UPDATE sys_menu SET menu_code = 'finance.transactions' WHERE menu_code = 'finance_transactions';
UPDATE sys_menu SET menu_code = 'finance.recharges' WHERE menu_code = 'finance_recharges';

UPDATE sys_menu SET menu_code = 'system.pricing' WHERE menu_code = 'system_pricing';
UPDATE sys_menu SET menu_code = 'system.walletTypes' WHERE menu_code = 'system_wallet_types';
UPDATE sys_menu SET menu_code = 'system.customerService' WHERE menu_code = 'system_customer_service';
UPDATE sys_menu SET menu_code = 'system.mail' WHERE menu_code = 'system_mail';
UPDATE sys_menu SET menu_code = 'system.regions' WHERE menu_code = 'system_regions';
UPDATE sys_menu SET menu_code = 'system.adminAccounts' WHERE menu_code = 'system_admin_accounts';
UPDATE sys_menu SET menu_code = 'system.roles' WHERE menu_code = 'system_roles';
UPDATE sys_menu SET menu_code = 'system.settings' WHERE menu_code = 'system_settings';
