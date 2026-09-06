CREATE TABLE admin_account_menu (
    admin_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (admin_id, menu_id),
    CONSTRAINT fk_admin_account_menu_admin FOREIGN KEY (admin_id) REFERENCES admin_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_account_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
);

INSERT IGNORE INTO admin_account_menu (admin_id, menu_id)
SELECT ar.admin_id, rm.menu_id
FROM admin_account_role ar
JOIN sys_role r ON r.id = ar.role_id AND r.status = 'ENABLED'
JOIN sys_role_menu rm ON rm.role_id = r.id;
