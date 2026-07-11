UPDATE admin_account
SET force_password_change = 0
WHERE username = 'superadmin'
  AND email = 'admin@youou-aso.local';
