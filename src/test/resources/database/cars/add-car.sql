DELETE FROM cars;
ALTER TABLE cars AUTO_INCREMENT = 1;
INSERT INTO cars (id, model, brand, type, inventory, daily_fee, is_deleted) VALUES (1, 'Q8', 'Audi', 'SUV', 10, 150.00, 0);
