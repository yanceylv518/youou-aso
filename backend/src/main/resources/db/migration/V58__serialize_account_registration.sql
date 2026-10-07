-- Serialize customer/admin registration in one transaction across all application instances.
CREATE TABLE account_registration_lock (id INT PRIMARY KEY);
INSERT INTO account_registration_lock (id) VALUES (1);
