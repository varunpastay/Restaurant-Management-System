-- =============================================================================
-- Demo seed data for a single restaurant deployment.
-- Run schema.sql first, then this file, against the same database.
--
-- IMPORTANT: admin/staff password_hash + password_salt below are placeholders
-- ('SEED_PLACEHOLDER...'), NOT usable credentials. Module 4 ships
-- com.restro.utility.PasswordHashGeneratorTool - run it once to generate a
-- real PBKDF2 hash+salt for your chosen password, then UPDATE these rows
-- (see docs/README.md, "First-time admin password setup").
-- =============================================================================

USE restaurant_db;

-- -----------------------------------------------------------------------------
-- restaurant (1 row per deployment)
-- -----------------------------------------------------------------------------
INSERT INTO restaurant
    (name, logo_path, banner_path, address, phone, email, gstin,
     currency_code, currency_symbol, service_charge_percent, theme_color,
     dark_mode_default, opening_time, closing_time, is_open)
VALUES
    ('Spice Route Bistro', '/uploads/branding/logo.png', '/uploads/branding/banner.jpg',
     '221B Residency Road, Bengaluru, Karnataka 560025', '+91-9876543210',
     'contact@spicerouteBistro.example', '29ABCDE1234F1Z5',
     'INR', '₹', 5.00, '#c0392b', 0, '10:00:00', '23:00:00', 1);

SET @restaurant_id = LAST_INSERT_ID();

-- -----------------------------------------------------------------------------
-- admin
-- -----------------------------------------------------------------------------
INSERT INTO admin (restaurant_id, email, password_hash, password_salt, full_name, is_active)
VALUES
    (@restaurant_id, 'owner@spicerouteBistro.example', 'SEED_PLACEHOLDER_HASH', 'SEED_PLACEHOLDER_SALT',
     'Restaurant Owner', 1);

-- -----------------------------------------------------------------------------
-- staff (one kitchen login, one counter login)
-- -----------------------------------------------------------------------------
INSERT INTO staff (restaurant_id, email, password_hash, password_salt, full_name, role, phone, is_active)
VALUES
    (@restaurant_id, 'kitchen@spicerouteBistro.example', 'SEED_PLACEHOLDER_HASH', 'SEED_PLACEHOLDER_SALT',
     'Kitchen Staff', 'KITCHEN', '+91-9000000001', 1),
    (@restaurant_id, 'counter@spicerouteBistro.example', 'SEED_PLACEHOLDER_HASH', 'SEED_PLACEHOLDER_SALT',
     'Counter Staff', 'COUNTER', '+91-9000000002', 1);

-- -----------------------------------------------------------------------------
-- tax (CGST + SGST = 5% total, standard Indian restaurant GST split)
-- -----------------------------------------------------------------------------
INSERT INTO tax (restaurant_id, name, percent, is_active)
VALUES
    (@restaurant_id, 'CGST', 2.50, 1),
    (@restaurant_id, 'SGST', 2.50, 1);

-- -----------------------------------------------------------------------------
-- discount (one sample manual/coupon-style discount)
-- -----------------------------------------------------------------------------
INSERT INTO discount (restaurant_id, code, description, discount_type, value, is_active)
VALUES
    (@restaurant_id, 'WELCOME10', 'First visit 10% off', 'PERCENT', 10.00, 1);

SET @discount_welcome10 = LAST_INSERT_ID();

-- -----------------------------------------------------------------------------
-- settings (misc toggles not worth a dedicated restaurant column)
-- -----------------------------------------------------------------------------
INSERT INTO settings (restaurant_id, setting_key, setting_value)
VALUES
    (@restaurant_id, 'notification_sound_enabled', 'true'),
    (@restaurant_id, 'invoice_footer_note', 'Thank you for dining with us! Visit again.');

-- -----------------------------------------------------------------------------
-- category (display_order controls menu ordering)
-- -----------------------------------------------------------------------------
INSERT INTO category (restaurant_id, name, display_order, image_path, is_active) VALUES
    (@restaurant_id, 'Starters',      1, '/uploads/categories/starters.jpg',      1),
    (@restaurant_id, 'South Indian',  2, '/uploads/categories/south-indian.jpg',  1),
    (@restaurant_id, 'North Indian',  3, '/uploads/categories/north-indian.jpg',  1),
    (@restaurant_id, 'Chinese',       4, '/uploads/categories/chinese.jpg',       1),
    (@restaurant_id, 'Beverages',     5, '/uploads/categories/beverages.jpg',     1),
    (@restaurant_id, 'Desserts',      6, '/uploads/categories/desserts.jpg',      1);

-- Category ids are known: Starters=1, South Indian=2, North Indian=3, Chinese=4, Beverages=5, Desserts=6
-- (fresh database, sequential auto-increment starting at 1).

-- -----------------------------------------------------------------------------
-- food_item (17 demo items across all 6 categories)
-- -----------------------------------------------------------------------------
INSERT INTO food_item
    (restaurant_id, category_id, name, description, ingredients, price, offer_price,
     prep_time_minutes, food_type, spice_level, is_available, is_recommended, is_bestseller, display_order)
VALUES
    (@restaurant_id, 1, 'Paneer Tikka', 'Chargrilled cottage cheese marinated in spiced yogurt.',
        'Paneer, yogurt, bell pepper, onion, tikka spices', 220.00, 199.00, 20, 'VEG', 'MEDIUM', 1, 1, 0, 1),
    (@restaurant_id, 1, 'Chicken 65', 'Deep-fried spicy chicken bites, South Indian style.',
        'Chicken, curry leaves, red chili, yogurt', 260.00, NULL, 20, 'NON_VEG', 'HOT', 1, 0, 1, 2),
    (@restaurant_id, 1, 'Veg Spring Rolls', 'Crispy rolls stuffed with mixed vegetables.',
        'Cabbage, carrot, spring roll sheet, soy sauce', 180.00, NULL, 15, 'VEG', 'MILD', 1, 0, 0, 3),

    (@restaurant_id, 2, 'Masala Dosa', 'Crisp rice crepe filled with spiced potato masala.',
        'Rice batter, potato, mustard seeds, curry leaves', 120.00, NULL, 15, 'VEG', 'MILD', 1, 0, 1, 1),
    (@restaurant_id, 2, 'Idli Sambar', 'Steamed rice cakes served with lentil sambar.',
        'Rice, urad dal, sambar lentils, vegetables', 90.00, NULL, 10, 'VEG', 'MILD', 1, 0, 0, 2),
    (@restaurant_id, 2, 'Uttapam', 'Thick savory pancake topped with onion and tomato.',
        'Rice batter, onion, tomato, green chili', 130.00, NULL, 15, 'VEG', 'MEDIUM', 1, 0, 0, 3),

    (@restaurant_id, 3, 'Butter Chicken', 'Tandoori chicken simmered in creamy tomato gravy.',
        'Chicken, tomato, butter, cream, fenugreek', 320.00, NULL, 25, 'NON_VEG', 'MEDIUM', 1, 1, 1, 1),
    (@restaurant_id, 3, 'Dal Makhani', 'Slow-cooked black lentils with butter and cream.',
        'Black lentils, kidney beans, butter, cream', 210.00, NULL, 30, 'VEG', 'MILD', 1, 0, 0, 2),
    (@restaurant_id, 3, 'Paneer Butter Masala', 'Cottage cheese in rich, mildly spiced tomato gravy.',
        'Paneer, tomato, butter, cashew paste', 260.00, 240.00, 20, 'VEG', 'MEDIUM', 1, 1, 0, 3),

    (@restaurant_id, 4, 'Veg Fried Rice', 'Wok-tossed rice with fresh vegetables.',
        'Rice, carrot, beans, spring onion, soy sauce', 190.00, NULL, 15, 'VEG', 'MEDIUM', 1, 0, 0, 1),
    (@restaurant_id, 4, 'Chicken Manchurian', 'Indo-Chinese fried chicken in tangy sauce.',
        'Chicken, garlic, soy sauce, spring onion', 270.00, NULL, 20, 'NON_VEG', 'HOT', 1, 0, 0, 2),
    (@restaurant_id, 4, 'Chilli Paneer', 'Cottage cheese tossed in spicy Indo-Chinese sauce.',
        'Paneer, capsicum, soy sauce, red chili', 230.00, NULL, 18, 'VEG', 'HOT', 1, 0, 0, 3),

    (@restaurant_id, 5, 'Masala Chai', 'Spiced Indian milk tea.',
        'Tea leaves, milk, cardamom, ginger', 40.00, NULL, 5, 'VEG', 'MILD', 1, 0, 0, 1),
    (@restaurant_id, 5, 'Fresh Lime Soda', 'Chilled soda with fresh lime, sweet or salted.',
        'Lime, soda water, sugar/salt', 60.00, NULL, 5, 'VEG', 'MILD', 1, 0, 0, 2),
    (@restaurant_id, 5, 'Cold Coffee', 'Blended chilled coffee with milk and ice cream.',
        'Coffee, milk, sugar, ice cream', 90.00, NULL, 8, 'VEG', 'MILD', 1, 0, 0, 3),

    (@restaurant_id, 6, 'Gulab Jamun', 'Soft milk-solid dumplings soaked in rose-cardamom syrup.',
        'Milk solids, sugar syrup, cardamom, rose water', 80.00, NULL, 5, 'VEG', 'MILD', 1, 0, 1, 1),
    (@restaurant_id, 6, 'Chocolate Brownie', 'Warm fudge brownie served with a scoop of vanilla ice cream.',
        'Chocolate, flour, butter, vanilla ice cream', 150.00, NULL, 10, 'VEG', 'MILD', 1, 1, 0, 2);

-- food_item ids are sequential 1..17 in the order inserted above.

-- -----------------------------------------------------------------------------
-- food_image (one primary photo per item)
-- -----------------------------------------------------------------------------
INSERT INTO food_image (food_item_id, image_path, is_primary, display_order)
SELECT food_item_id,
       CONCAT('/uploads/food/', LOWER(REPLACE(REPLACE(name, ' ', '-'), '/', '-')), '.jpg'),
       1, 1
FROM food_item
WHERE restaurant_id = @restaurant_id;

-- -----------------------------------------------------------------------------
-- restaurant_table (6 tables, unique QR tokens)
-- -----------------------------------------------------------------------------
INSERT INTO restaurant_table (restaurant_id, table_no, capacity, qr_token, is_active) VALUES
    (@restaurant_id, '1', 4, 'a1e6f9c2b3d84e0f9a1c2b3d4e5f6071', 1),
    (@restaurant_id, '2', 4, 'b2f7a0d3c4e95f1a0b2d3c4e5f607182', 1),
    (@restaurant_id, '3', 2, 'c3a8b1e4d5fa6021b3e4d5f6a7b80293', 1),
    (@restaurant_id, '4', 6, 'd4b9c2f5e6ab7132c4f5e6a7b8c9304a', 1),
    (@restaurant_id, '5', 4, 'e5cad3a6f7bc8243d5a6f7b8c9da415b', 1),
    (@restaurant_id, '6', 8, 'f6dbe4b7a8cd9354e6b7a8c9dae5266c', 1);

-- restaurant_table ids are sequential 1..6 in the order inserted above.

-- =============================================================================
-- Sample historical orders, spread across the last few days, so the Sales
-- Reports module (peak hours / top sellers / revenue graphs) has real data
-- to render immediately without requiring manual test orders first.
-- =============================================================================

-- Order 1: 3 days ago, lunch, table 1, COMPLETED + paid CASH
INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, tax_amount,
                     service_charge_amount, discount_amount, grand_total, created_at, updated_at)
VALUES (@restaurant_id,
        CONCAT('ORD-', DATE_FORMAT(CURDATE() - INTERVAL 3 DAY, '%Y%m%d'), '-0001'),
        1, 'COMPLETED', 400.00, 20.00, 20.00, 0.00, 440.00,
        TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '13:05:00'), TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '13:45:00'));
SET @order1 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, quantity, line_total) VALUES
    (@order1, 7, 'Butter Chicken', 320.00, 1, 320.00),
    (@order1, 13, 'Masala Chai', 40.00, 2, 80.00);
INSERT INTO order_status_history (order_id, status, changed_at) VALUES
    (@order1, 'PENDING', TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '13:05:00')),
    (@order1, 'SERVED', TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '13:30:00')),
    (@order1, 'COMPLETED', TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '13:45:00'));
INSERT INTO payment (order_id, invoice_no, amount, method, payment_status, paid_at) VALUES
    (@order1, CONCAT('INV-', DATE_FORMAT(CURDATE() - INTERVAL 3 DAY, '%Y%m%d'), '-0001'),
     440.00, 'CASH', 'PAID', TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '13:45:00'));

-- Order 2: 3 days ago, dinner, table 2, COMPLETED + paid UPI, with WELCOME10 discount
INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, tax_amount,
                     service_charge_amount, discount_amount, grand_total, discount_id, created_at, updated_at)
VALUES (@restaurant_id,
        CONCAT('ORD-', DATE_FORMAT(CURDATE() - INTERVAL 3 DAY, '%Y%m%d'), '-0002'),
        2, 'COMPLETED', 630.00, 31.50, 31.50, 63.00, 630.00, @discount_welcome10,
        TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '20:15:00'), TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '21:00:00'));
SET @order2 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, quantity, line_total) VALUES
    (@order2, 9, 'Paneer Butter Masala', 240.00, 1, 240.00),
    (@order2, 8, 'Dal Makhani', 210.00, 1, 210.00),
    (@order2, 15, 'Cold Coffee', 90.00, 2, 180.00);
INSERT INTO order_status_history (order_id, status, changed_at) VALUES
    (@order2, 'PENDING', TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '20:15:00')),
    (@order2, 'SERVED', TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '20:45:00')),
    (@order2, 'COMPLETED', TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '21:00:00'));
INSERT INTO payment (order_id, invoice_no, amount, method, payment_status, paid_at) VALUES
    (@order2, CONCAT('INV-', DATE_FORMAT(CURDATE() - INTERVAL 3 DAY, '%Y%m%d'), '-0002'),
     630.00, 'UPI', 'PAID', TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '21:00:00'));

-- Order 3: 2 days ago, lunch, table 3, COMPLETED + paid CASH
INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, tax_amount,
                     service_charge_amount, discount_amount, grand_total, created_at, updated_at)
VALUES (@restaurant_id,
        CONCAT('ORD-', DATE_FORMAT(CURDATE() - INTERVAL 2 DAY, '%Y%m%d'), '-0001'),
        3, 'COMPLETED', 330.00, 16.50, 16.50, 0.00, 363.00,
        TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '12:40:00'), TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '13:10:00'));
SET @order3 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, quantity, line_total) VALUES
    (@order3, 4, 'Masala Dosa', 120.00, 2, 240.00),
    (@order3, 5, 'Idli Sambar', 90.00, 1, 90.00);
INSERT INTO order_status_history (order_id, status, changed_at) VALUES
    (@order3, 'PENDING', TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '12:40:00')),
    (@order3, 'SERVED', TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '13:00:00')),
    (@order3, 'COMPLETED', TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '13:10:00'));
INSERT INTO payment (order_id, invoice_no, amount, method, payment_status, paid_at) VALUES
    (@order3, CONCAT('INV-', DATE_FORMAT(CURDATE() - INTERVAL 2 DAY, '%Y%m%d'), '-0001'),
     363.00, 'CASH', 'PAID', TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '13:10:00'));

-- Order 4: 2 days ago, dinner, table 4, COMPLETED + paid CARD
INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, tax_amount,
                     service_charge_amount, discount_amount, grand_total, created_at, updated_at)
VALUES (@restaurant_id,
        CONCAT('ORD-', DATE_FORMAT(CURDATE() - INTERVAL 2 DAY, '%Y%m%d'), '-0002'),
        4, 'COMPLETED', 650.00, 32.50, 32.50, 0.00, 715.00,
        TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '19:50:00'), TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '20:30:00'));
SET @order4 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, quantity, line_total) VALUES
    (@order4, 2, 'Chicken 65', 260.00, 1, 260.00),
    (@order4, 11, 'Chicken Manchurian', 270.00, 1, 270.00),
    (@order4, 14, 'Fresh Lime Soda', 60.00, 2, 120.00);
INSERT INTO order_status_history (order_id, status, changed_at) VALUES
    (@order4, 'PENDING', TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '19:50:00')),
    (@order4, 'SERVED', TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '20:20:00')),
    (@order4, 'COMPLETED', TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '20:30:00'));
INSERT INTO payment (order_id, invoice_no, amount, method, payment_status, paid_at) VALUES
    (@order4, CONCAT('INV-', DATE_FORMAT(CURDATE() - INTERVAL 2 DAY, '%Y%m%d'), '-0002'),
     715.00, 'CARD', 'PAID', TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '20:30:00'));

-- Order 5: yesterday, lunch, table 5, COMPLETED + paid UPI
INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, tax_amount,
                     service_charge_amount, discount_amount, grand_total, created_at, updated_at)
VALUES (@restaurant_id,
        CONCAT('ORD-', DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d'), '-0001'),
        5, 'COMPLETED', 580.00, 29.00, 29.00, 0.00, 638.00,
        TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '13:20:00'), TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '13:55:00'));
SET @order5 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, quantity, line_total) VALUES
    (@order5, 10, 'Veg Fried Rice', 190.00, 1, 190.00),
    (@order5, 12, 'Chilli Paneer', 230.00, 1, 230.00),
    (@order5, 16, 'Gulab Jamun', 80.00, 2, 160.00);
INSERT INTO order_status_history (order_id, status, changed_at) VALUES
    (@order5, 'PENDING', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '13:20:00')),
    (@order5, 'SERVED', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '13:45:00')),
    (@order5, 'COMPLETED', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '13:55:00'));
INSERT INTO payment (order_id, invoice_no, amount, method, payment_status, paid_at) VALUES
    (@order5, CONCAT('INV-', DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d'), '-0001'),
     638.00, 'UPI', 'PAID', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '13:55:00'));

-- Order 6: yesterday, dinner, table 6, COMPLETED + paid CASH
INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, tax_amount,
                     service_charge_amount, discount_amount, grand_total, created_at, updated_at)
VALUES (@restaurant_id,
        CONCAT('ORD-', DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d'), '-0002'),
        6, 'COMPLETED', 790.00, 39.50, 39.50, 0.00, 869.00,
        TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '21:05:00'), TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '21:50:00'));
SET @order6 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, quantity, line_total) VALUES
    (@order6, 7, 'Butter Chicken', 320.00, 2, 640.00),
    (@order6, 17, 'Chocolate Brownie', 150.00, 1, 150.00);
INSERT INTO order_status_history (order_id, status, changed_at) VALUES
    (@order6, 'PENDING', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '21:05:00')),
    (@order6, 'SERVED', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '21:35:00')),
    (@order6, 'COMPLETED', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '21:50:00'));
INSERT INTO payment (order_id, invoice_no, amount, method, payment_status, paid_at) VALUES
    (@order6, CONCAT('INV-', DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d'), '-0002'),
     869.00, 'CASH', 'PAID', TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '21:50:00'));

-- Order 7: today, lunch, table 2, already SERVED but not yet billed (no payment row = unpaid)
INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, tax_amount,
                     service_charge_amount, discount_amount, grand_total, created_at, updated_at)
VALUES (@restaurant_id,
        CONCAT('ORD-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-0001'),
        2, 'SERVED', 170.00, 8.50, 8.50, 0.00, 187.00,
        TIMESTAMP(CURDATE(), '12:50:00'), TIMESTAMP(CURDATE(), '13:10:00'));
SET @order7 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, quantity, line_total) VALUES
    (@order7, 6, 'Uttapam', 130.00, 1, 130.00),
    (@order7, 13, 'Masala Chai', 40.00, 1, 40.00);
INSERT INTO order_status_history (order_id, status, changed_at) VALUES
    (@order7, 'PENDING', TIMESTAMP(CURDATE(), '12:50:00')),
    (@order7, 'SERVED', TIMESTAMP(CURDATE(), '13:10:00'));

-- Order 8: today, a cancelled order, table 3
INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, tax_amount,
                     service_charge_amount, discount_amount, grand_total, created_at, updated_at)
VALUES (@restaurant_id,
        CONCAT('ORD-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-0002'),
        3, 'CANCELLED', 440.00, 22.00, 22.00, 0.00, 484.00,
        TIMESTAMP(CURDATE(), '13:05:00'), TIMESTAMP(CURDATE(), '13:08:00'));
SET @order8 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, quantity, line_total) VALUES
    (@order8, 2, 'Chicken 65', 260.00, 1, 260.00),
    (@order8, 3, 'Veg Spring Rolls', 180.00, 1, 180.00);
INSERT INTO order_status_history (order_id, status, changed_at) VALUES
    (@order8, 'PENDING', TIMESTAMP(CURDATE(), '13:05:00')),
    (@order8, 'CANCELLED', TIMESTAMP(CURDATE(), '13:08:00'));

-- Order 9: today, still live in the kitchen queue, table 1 - PENDING, no payment
INSERT INTO orders (restaurant_id, order_no, table_id, status, subtotal, tax_amount,
                     service_charge_amount, discount_amount, grand_total, created_at, updated_at)
VALUES (@restaurant_id,
        CONCAT('ORD-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-0003'),
        1, 'PENDING', 289.00, 14.45, 14.45, 0.00, 317.90,
        NOW(), NOW());
SET @order9 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, food_item_id, food_name_snapshot, unit_price, quantity, line_total) VALUES
    (@order9, 1, 'Paneer Tikka', 199.00, 1, 199.00),
    (@order9, 15, 'Cold Coffee', 90.00, 1, 90.00);
INSERT INTO order_status_history (order_id, status, changed_at) VALUES
    (@order9, 'PENDING', NOW());
