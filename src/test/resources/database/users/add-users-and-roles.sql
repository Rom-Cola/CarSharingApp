DELETE FROM users_roles;
DELETE FROM users;
DELETE FROM roles;

ALTER TABLE roles AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;

INSERT INTO roles (id, name) VALUES (1, 'MANAGER'), (2, 'CUSTOMER');
INSERT INTO users (id, email, password, first_name, last_name, is_deleted) VALUES (1, 'customer@example.com', 'password1.somesalt/somehash', 'John', 'Doe', 0), (2, 'manager@example.com', 'password2.somesalt/somehash', 'Admin', 'Admin', 0);
INSERT INTO users_roles (user_id, role_id) VALUES (1, 2), (2, 1);
