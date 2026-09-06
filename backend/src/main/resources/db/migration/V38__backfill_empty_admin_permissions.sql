INSERT IGNORE INTO admin_account_menu (admin_id, menu_id)
SELECT a.id, rm.menu_id
FROM admin_account a
JOIN admin_account_role ar ON ar.admin_id = a.id
JOIN sys_role r ON r.id = ar.role_id AND r.status = 'ENABLED'
JOIN sys_role_menu rm ON rm.role_id = r.id
WHERE a.role_code <> 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM admin_account_menu am WHERE am.admin_id = a.id);

INSERT IGNORE INTO admin_account_menu (admin_id, menu_id)
SELECT a.id, rm.menu_id
FROM admin_account a
JOIN sys_role r ON r.role_key = 'OPERATOR' AND r.status = 'ENABLED'
JOIN sys_role_menu rm ON rm.role_id = r.id
WHERE a.role_code <> 'SUPER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM admin_account_menu am WHERE am.admin_id = a.id);
