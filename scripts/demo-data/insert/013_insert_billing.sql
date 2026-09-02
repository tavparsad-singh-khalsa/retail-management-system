-- retail_billing (billing-service) - 15 invoices (one per sale) + 33 items
BEGIN;

INSERT INTO invoices (id, invoice_number, sale_id, customer_id, subtotal, tax_amount, discount_amount, total, currency, invoice_status, payment_method, payment_status, transaction_reference, paid_amount, paid_at, created_at, updated_at) VALUES
(1,  'INV-2026-000001',  1, 1, 5097.00, 0.00, 0.00, 5097.00, 'INR', 'PAID',      'CASH',          'PAID',    'TXN-2026-0001', 5097.00, TIMESTAMP '2026-06-02 12:20:00', TIMESTAMP '2026-06-02 12:20:00', TIMESTAMP '2026-06-02 12:20:00'),
(2,  'INV-2026-000002',  2, 2, 2148.00, 0.00, 0.00, 2148.00, 'INR', 'PAID',      'UPI',           'PAID',    'TXN-2026-0002', 2148.00, TIMESTAMP '2026-06-05 14:35:00', TIMESTAMP '2026-06-05 14:35:00', TIMESTAMP '2026-06-05 14:35:00'),
(3,  'INV-2026-000003',  3, 3, 2198.00, 0.00, 0.00, 2198.00, 'INR', 'PAID',      'CARD',          'PAID',    'TXN-2026-0003', 2198.00, TIMESTAMP '2026-06-09 11:10:00', TIMESTAMP '2026-06-09 11:10:00', TIMESTAMP '2026-06-09 11:10:00'),
(4,  'INV-2026-000004',  4, 4, 1948.00, 0.00, 0.00, 1948.00, 'INR', 'PAID',      'UPI',           'PAID',    'TXN-2026-0004', 1948.00, TIMESTAMP '2026-06-12 18:50:00', TIMESTAMP '2026-06-12 18:50:00', TIMESTAMP '2026-06-12 18:50:00'),
(5,  'INV-2026-000005',  5, 5, 3798.00, 0.00, 0.00, 3798.00, 'INR', 'PAID',      'CASH',          'PAID',    'TXN-2026-0005', 3798.00, TIMESTAMP '2026-06-15 13:25:00', TIMESTAMP '2026-06-15 13:25:00', TIMESTAMP '2026-06-15 13:25:00'),
(6,  'INV-2026-000006',  6, 6, 2247.00, 0.00, 0.00, 2247.00, 'INR', 'PAID',      'UPI',           'PAID',    'TXN-2026-0006', 2247.00, TIMESTAMP '2026-06-19 16:15:00', TIMESTAMP '2026-06-19 16:15:00', TIMESTAMP '2026-06-19 16:15:00'),
(7,  'INV-2026-000007',  7, 7, 2948.00, 0.00, 0.00, 2948.00, 'INR', 'PAID',      'CARD',          'PAID',    'TXN-2026-0007', 2948.00, TIMESTAMP '2026-06-23 12:45:00', TIMESTAMP '2026-06-23 12:45:00', TIMESTAMP '2026-06-23 12:45:00'),
(8,  'INV-2026-000008',  8, 8, 2498.00, 0.00, 0.00, 2498.00, 'INR', 'PAID',      'CASH',          'PAID',    'TXN-2026-0008', 2498.00, TIMESTAMP '2026-06-27 16:00:00', TIMESTAMP '2026-06-27 16:00:00', TIMESTAMP '2026-06-27 16:00:00'),
(9,  'INV-2026-000009',  9, 9, 1698.00, 0.00, 0.00, 1698.00, 'INR', 'PAID',      'UPI',           'PAID',    'TXN-2026-0009', 1698.00, TIMESTAMP '2026-07-02 11:30:00', TIMESTAMP '2026-07-02 11:30:00', TIMESTAMP '2026-07-02 11:30:00'),
(10, 'INV-2026-000010', 10, 10, 5697.00, 0.00, 0.00, 5697.00, 'INR', 'PAID',      'CARD',          'PAID',    'TXN-2026-0010', 5697.00, TIMESTAMP '2026-07-06 17:40:00', TIMESTAMP '2026-07-06 17:40:00', TIMESTAMP '2026-07-06 17:40:00'),
(11, 'INV-2026-000011', 11, 1, 3398.00, 0.00, 0.00, 3398.00, 'INR', 'PAID',      'CASH',          'PAID',    'TXN-2026-0011', 3398.00, TIMESTAMP '2026-07-10 12:10:00', TIMESTAMP '2026-07-10 12:10:00', TIMESTAMP '2026-07-10 12:10:00'),
(12, 'INV-2026-000012', 12, 2, 1948.00, 0.00, 0.00, 1948.00, 'INR', 'PAID',      'UPI',           'PAID',    'TXN-2026-0012', 1948.00, TIMESTAMP '2026-07-15 14:55:00', TIMESTAMP '2026-07-15 14:55:00', TIMESTAMP '2026-07-15 14:55:00'),
(13, 'INV-2026-000013', 13, 3, 3898.00, 0.00, 0.00, 3898.00, 'INR', 'PAID',      'CARD',          'PAID',    'TXN-2026-0013', 3898.00, TIMESTAMP '2026-07-20 13:20:00', TIMESTAMP '2026-07-20 13:20:00', TIMESTAMP '2026-07-20 13:20:00'),
(14, 'INV-2026-000014', 14, 4, 1148.00, 0.00, 0.00, 1148.00, 'INR', 'GENERATED', NULL,            'PENDING', NULL,            0.00,    NULL,                       TIMESTAMP '2026-07-25 18:30:00', TIMESTAMP '2026-07-25 18:30:00'),
(15, 'INV-2026-000015', 15, 5, 2997.00, 0.00, 0.00, 2997.00, 'INR', 'GENERATED', NULL,            'PENDING', NULL,            0.00,    NULL,                       TIMESTAMP '2026-07-30 19:10:00', TIMESTAMP '2026-07-30 19:10:00');

-- invoice items (mirror sale items)
INSERT INTO invoice_items (id, invoice_id, product_id, variant_id, sku, barcode, product_name, variant_name, quantity, unit_price, discount_amount, tax_amount, line_total) VALUES
(1,  1, 1,  2,  'SKU-2026-000002', 'BAR-2026-000002', 'Nike Men''s Polo T-Shirt',    'Nike Men''s Polo T-Shirt - Size M',     1,  799.00, 0.00, 0.00,  799.00),
(2,  1, 10, 19, 'SKU-2026-000019', 'BAR-2026-000019', 'Nike Running Shoes',          'Nike Running Shoes - Size UK 8',       1, 3499.00, 0.00, 0.00, 3499.00),
(3,  1, 13, 25, 'SKU-2026-000025', 'BAR-2026-000025', 'Levi''s Leather Belt',         'Levi''s Leather Belt - Black',         1,  799.00, 0.00, 0.00,  799.00),
(4,  2, 2,  3,  'SKU-2026-000003', 'BAR-2026-000003', 'Levi''s Men''s Denim Jeans',  'Levi''s Men''s Denim Jeans - Size 32', 1, 1699.00, 0.00, 0.00, 1699.00),
(5,  2, 14, 27, 'SKU-2026-000027', 'BAR-2026-000027', 'Allen Solly Trucker Cap',     'Allen Solly Trucker Cap - Navy',       1,  449.00, 0.00, 0.00,  449.00),
(6,  3, 3,  5,  'SKU-2026-000005', 'BAR-2026-000005', 'Puma Men''s Track Pants',     'Puma Men''s Track Pants - Size M',     1, 1199.00, 0.00, 0.00, 1199.00),
(7,  3, 6,  11, 'SKU-2026-000011', 'BAR-2026-000011', 'Nike Women''s Leggings',      'Nike Women''s Leggings - Size M',      1,  999.00, 0.00, 0.00,  999.00),
(8,  4, 4,  7,  'SKU-2026-000007', 'BAR-2026-000007', 'Allen Solly Women''s Kurta',  'Allen Solly Women''s Kurta - Size M',  1, 1399.00, 0.00, 0.00, 1399.00),
(9,  4, 15, 29, 'SKU-2026-000029', 'BAR-2026-000029', 'Nike Performance Cap',        'Nike Performance Cap - Black',         1,  549.00, 0.00, 0.00,  549.00),
(10, 5, 11, 21, 'SKU-2026-000021', 'BAR-2026-000021', 'Adidas Street Sneakers',      'Adidas Street Sneakers - Size UK 8',   1, 2999.00, 0.00, 0.00, 2999.00),
(11, 5, 1,  1,  'SKU-2026-000001', 'BAR-2026-000001', 'Nike Men''s Polo T-Shirt',    'Nike Men''s Polo T-Shirt - Size S',    1,  799.00, 0.00, 0.00,  799.00),
(12, 6, 5,  9,  'SKU-2026-000009', 'BAR-2026-000009', 'Adidas Women''s Sports Top',  'Adidas Women''s Sports Top - Size S',  1, 1099.00, 0.00, 0.00, 1099.00),
(13, 6, 7,  13, 'SKU-2026-000013', 'BAR-2026-000013', 'Puma Kids T-Shirt',           'Puma Kids T-Shirt - Size 6-7 Years',   1,  599.00, 0.00, 0.00,  599.00),
(14, 6, 8,  15, 'SKU-2026-000015', 'BAR-2026-000015', 'Nike Kids Shorts',            'Nike Kids Shorts - Size 6-7 Years',    1,  549.00, 0.00, 0.00,  549.00),
(15, 7, 12, 23, 'SKU-2026-000023', 'BAR-2026-000023', 'Puma Sports Shoes',           'Puma Sports Shoes - Size UK 8',        1, 2499.00, 0.00, 0.00, 2499.00),
(16, 7, 14, 28, 'SKU-2026-000028', 'BAR-2026-000028', 'Allen Solly Trucker Cap',     'Allen Solly Trucker Cap - Grey',       1,  449.00, 0.00, 0.00,  449.00),
(17, 8, 2,  4,  'SKU-2026-000004', 'BAR-2026-000004', 'Levi''s Men''s Denim Jeans',  'Levi''s Men''s Denim Jeans - Size 34', 1, 1699.00, 0.00, 0.00, 1699.00),
(18, 8, 13, 26, 'SKU-2026-000026', 'BAR-2026-000026', 'Levi''s Leather Belt',         'Levi''s Leather Belt - Brown',         1,  799.00, 0.00, 0.00,  799.00),
(19, 9, 5,  10, 'SKU-2026-000010', 'BAR-2026-000010', 'Adidas Women''s Sports Top',  'Adidas Women''s Sports Top - Size M',  1, 1099.00, 0.00, 0.00, 1099.00),
(20, 9, 7,  14, 'SKU-2026-000014', 'BAR-2026-000014', 'Puma Kids T-Shirt',           'Puma Kids T-Shirt - Size 8-9 Years',   1,  599.00, 0.00, 0.00,  599.00),
(21, 10, 3,  6,  'SKU-2026-000006', 'BAR-2026-000006', 'Puma Men''s Track Pants',     'Puma Men''s Track Pants - Size L',     1, 1199.00, 0.00, 0.00, 1199.00),
(22, 10, 6,  12, 'SKU-2026-000012', 'BAR-2026-000012', 'Nike Women''s Leggings',      'Nike Women''s Leggings - Size L',      1,  999.00, 0.00, 0.00,  999.00),
(23, 10, 10, 20, 'SKU-2026-000020', 'BAR-2026-000020', 'Nike Running Shoes',          'Nike Running Shoes - Size UK 9',       1, 3499.00, 0.00, 0.00, 3499.00),
(24, 11, 9,  17, 'SKU-2026-000017', 'BAR-2026-000017', 'Adidas Kids Hoodie',          'Adidas Kids Hoodie - Size 6-7 Years',  1,  899.00, 0.00, 0.00,  899.00),
(25, 11, 12, 24, 'SKU-2026-000024', 'BAR-2026-000024', 'Puma Sports Shoes',           'Puma Sports Shoes - Size UK 9',        1, 2499.00, 0.00, 0.00, 2499.00),
(26, 12, 4,  8,  'SKU-2026-000008', 'BAR-2026-000008', 'Allen Solly Women''s Kurta',  'Allen Solly Women''s Kurta - Size L',  1, 1399.00, 0.00, 0.00, 1399.00),
(27, 12, 8,  16, 'SKU-2026-000016', 'BAR-2026-000016', 'Nike Kids Shorts',            'Nike Kids Shorts - Size 8-9 Years',    1,  549.00, 0.00, 0.00,  549.00),
(28, 13, 9,  18, 'SKU-2026-000018', 'BAR-2026-000018', 'Adidas Kids Hoodie',          'Adidas Kids Hoodie - Size 8-9 Years',  1,  899.00, 0.00, 0.00,  899.00),
(29, 13, 11, 22, 'SKU-2026-000022', 'BAR-2026-000022', 'Adidas Street Sneakers',      'Adidas Street Sneakers - Size UK 9',   1, 2999.00, 0.00, 0.00, 2999.00),
(30, 14, 15, 30, 'SKU-2026-000030', 'BAR-2026-000030', 'Nike Performance Cap',        'Nike Performance Cap - White',         1,  549.00, 0.00, 0.00,  549.00),
(31, 14, 7,  13, 'SKU-2026-000013', 'BAR-2026-000013', 'Puma Kids T-Shirt',           'Puma Kids T-Shirt - Size 6-7 Years',   1,  599.00, 0.00, 0.00,  599.00),
(32, 15, 1,  2,  'SKU-2026-000002', 'BAR-2026-000002', 'Nike Men''s Polo T-Shirt',    'Nike Men''s Polo T-Shirt - Size M',    2,  799.00, 0.00, 0.00, 1598.00),
(33, 15, 4,  7,  'SKU-2026-000007', 'BAR-2026-000007', 'Allen Solly Women''s Kurta',  'Allen Solly Women''s Kurta - Size M',  1, 1399.00, 0.00, 0.00, 1399.00);

COMMIT;
