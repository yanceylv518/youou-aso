UPDATE sys_menu child
JOIN sys_menu parent ON parent.menu_code = 'applications'
SET child.parent_id = parent.id
WHERE child.permission_code IN ('app:add', 'app:delete');

UPDATE sys_menu child
JOIN sys_menu parent ON parent.menu_code = 'orders_pending_confirm'
SET child.parent_id = parent.id
WHERE child.permission_code = 'order:confirm';

UPDATE sys_menu child
JOIN sys_menu parent ON parent.menu_code = 'order_execution_pending'
SET child.parent_id = parent.id
WHERE child.permission_code = 'order:execute';

UPDATE sys_menu child
JOIN sys_menu parent ON parent.menu_code = 'orders'
SET child.parent_id = parent.id
WHERE child.permission_code IN ('order:cancel', 'order:export');

UPDATE sys_menu child
JOIN sys_menu parent ON parent.menu_code = 'finance_transactions'
SET child.parent_id = parent.id
WHERE child.permission_code = 'wallet:adjust';
