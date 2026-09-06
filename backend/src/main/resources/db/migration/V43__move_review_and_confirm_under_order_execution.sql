UPDATE sys_menu child
JOIN sys_menu parent ON parent.menu_code = 'orderExecution'
SET child.parent_id = parent.id,
    child.sort_order = CASE child.menu_code
        WHEN 'orders.pendingReview' THEN 10
        WHEN 'orders.pendingConfirm' THEN 20
        ELSE child.sort_order
    END
WHERE child.menu_code IN ('orders.pendingReview', 'orders.pendingConfirm');

UPDATE sys_menu
SET sort_order = CASE menu_code
    WHEN 'orderExecution.pending' THEN 30
    WHEN 'orderExecution.executing' THEN 40
    WHEN 'orderExecution.completed' THEN 50
    ELSE sort_order
END
WHERE menu_code IN (
    'orderExecution.pending',
    'orderExecution.executing',
    'orderExecution.completed'
);
