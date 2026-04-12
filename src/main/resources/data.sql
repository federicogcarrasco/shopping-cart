INSERT INTO users (username, password, role) VALUES
('admin', '$2a$10$7QwWMODpSXBCLpHLJiUMUOFkdvuQXjCQF0bFdmhVALuDhiULHPRQe', 'ADMIN'),
('john', '$2a$10$7QwWMODpSXBCLpHLJiUMUOFkdvuQXjCQF0bFdmhVALuDhiULHPRQe', 'USER'),
('jane', '$2a$10$7QwWMODpSXBCLpHLJiUMUOFkdvuQXjCQF0bFdmhVALuDhiULHPRQe', 'USER');
-- La password de los 3 usuarios es password123 (ya hasheada por ByCrypt)

INSERT INTO product_categories (name, discount) VALUES
('Electronics', 0.10),
('Clothing', 0.20),
('Food', 0.05),
('Books', 0.15),
('Sports', 0.00);

INSERT INTO products (name, price, category_id) VALUES
('Laptop',      1500.00, 1),
('Smartphone',   800.00, 1),
('Headphones',   150.00, 1),
('T-Shirt',       25.00, 2),
('Jeans',         60.00, 2),
('Jacket',       120.00, 2),
('Rice 1kg',       2.50, 3),
('Pasta 500g',     1.80, 3),
('Olive Oil',      8.00, 3),
('Clean Code',    35.00, 4),
('The Pragmatic Programmer', 40.00, 4),
('Football',      30.00, 5),
('Tennis Racket', 85.00, 5);