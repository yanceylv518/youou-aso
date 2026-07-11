INSERT INTO admin_account (
    username,
    email,
    password_hash,
    role_code,
    status,
    force_password_change,
    preferred_locale
)
VALUES (
    'superadmin',
    'admin@youou-aso.local',
    '$2a$10$bQU3QWZw.tptoGU9on55k.4IdhuP57A4GvGZB3xuMwYk0CB1lM4jG',
    'SUPER_ADMIN',
    'ENABLED',
    1,
    'zh-CN'
);
