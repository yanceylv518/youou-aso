UPDATE sys_menu
SET name_zh = '订单模块',
    name_en = 'Order Modules'
WHERE menu_code IN ('system_pricing', 'system.pricing')
   OR route_path = '/admin/pricing';

UPDATE sys_menu
SET name_zh = '管理订单模块',
    name_en = 'Manage order modules'
WHERE permission_code = 'pricing:update';
