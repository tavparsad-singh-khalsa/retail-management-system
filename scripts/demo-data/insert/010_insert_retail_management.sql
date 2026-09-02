-- =====================================================================
-- Fresh demo dataset for a clothing retail shop  (retail_management DB)
--  5 categories, 5 brands, 15 products, 30 variants, 30 inventory rows,
--  5 suppliers, 10 purchase orders (+items), stock movements (IN/OUT).
-- Inventory = purchases received - sales sold  (consistent audit trail).
-- =====================================================================

BEGIN;

-- ---------------------------------------------------------------------
-- CATEGORIES (5)
-- ---------------------------------------------------------------------
INSERT INTO categories (id, created_at, created_by, is_active, updated_at, updated_by, version, description, name, parent_id) VALUES
(1, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Men''s clothing and apparel', 'Men''s Wear', NULL),
(2, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Women''s clothing and apparel', 'Women''s Wear', NULL),
(3, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Clothing for children', 'Kids Wear', NULL),
(4, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Shoes, sneakers and sandals', 'Footwear', NULL),
(5, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Belts, caps, bags and accessories', 'Accessories', NULL);

-- ---------------------------------------------------------------------
-- BRANDS (5)
-- ---------------------------------------------------------------------
INSERT INTO brands (id, created_at, created_by, is_active, updated_at, updated_by, version, description, name) VALUES
(1, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Sportswear and athletic footwear', 'Nike'),
(2, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Sports apparel and shoes', 'Adidas'),
(3, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Athletic clothing and footwear', 'Puma'),
(4, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Denim and casual wear', 'Levi''s'),
(5, now() - interval '120 days', 1, true, now() - interval '1 day', 1, 0, 'Smart casual menswear and womenswear', 'Allen Solly');

-- ---------------------------------------------------------------------
-- SUPPLIERS (5)
-- ---------------------------------------------------------------------
INSERT INTO suppliers (id, created_at, created_by, is_active, updated_at, updated_by, version, address, contact_person, email, gst_number, name, phone) VALUES
(1, now() - interval '110 days', 1, true, now() - interval '1 day', 1, 0, 'Plot 12, MIDC Andheri East, Mumbai, Maharashtra', 'Rajesh Kumar', 'sales@globaltextiles.in', '27AAACG1234A1Z5', 'Global Textiles Pvt Ltd', '9820010001'),
(2, now() - interval '110 days', 1, true, now() - interval '1 day', 1, 0, 'B-45, Okhla Industrial Area, New Delhi', 'Anita Sharma', 'info@shreefashion.in', '27AAHFS5678B1Z6', 'Shree Fashion Distributors', '9820010002'),
(3, now() - interval '110 days', 1, true, now() - interval '1 day', 1, 0, '38, T Nagar, Chennai, Tamil Nadu', 'Sunil Gupta', 'orders@metrofootwear.in', '27AAHMF9012C1Z7', 'Metro Footwear Wholesale', '9820010003'),
(4, now() - interval '110 days', 1, true, now() - interval '1 day', 1, 0, 'KR Puram Industrial Estate, Bengaluru, Karnataka', 'Priya Nair', 'contact@apexgarments.in', '27AAHAG3456D1Z8', 'Apex Garments Trading Co', '9820010004'),
(5, now() - interval '110 days', 1, true, now() - interval '1 day', 1, 0, 'College Street Market, Kolkata, West Bengal', 'Mohammed Ali', 'hello@stylepoint.in', '27AASPA7890E1Z9', 'Style Point Accessories', '9820010005');

-- ---------------------------------------------------------------------
-- PRODUCTS (15)
-- ---------------------------------------------------------------------
INSERT INTO products (id, created_at, created_by, is_active, updated_at, updated_by, version, description, has_variants, name, product_code, brand_id, category_id) VALUES
(1,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Classic cotton polo shirt for men', true, 'Nike Men''s Polo T-Shirt',      'PRD-2026-000001', 1, 1),
(2,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Slim-fit stretch denim jeans',   true, 'Levi''s Men''s Denim Jeans',      'PRD-2026-000002', 4, 1),
(3,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Comfortable fleece track pants', true, 'Puma Men''s Track Pants',        'PRD-2026-000003', 3, 1),
(4,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Cotton printed kurta for women', true, 'Allen Solly Women''s Kurta',     'PRD-2026-000004', 5, 2),
(5,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Breathable sports top',          true, 'Adidas Women''s Sports Top',     'PRD-2026-000005', 2, 2),
(6,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'High-waist yoga leggings',       true, 'Nike Women''s Leggings',         'PRD-2026-000006', 1, 2),
(7,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Soft cotton t-shirt for kids',   true, 'Puma Kids T-Shirt',              'PRD-2026-000007', 3, 3),
(8,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Quick-dry shorts for kids',      true, 'Nike Kids Shorts',               'PRD-2026-000008', 1, 3),
(9,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Warm pullover hoodie for kids',  true, 'Adidas Kids Hoodie',             'PRD-2026-000009', 2, 3),
(10, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Lightweight running shoes',      true, 'Nike Running Shoes',             'PRD-2026-000010', 1, 4),
(11, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Street-style casual sneakers',   true, 'Adidas Street Sneakers',         'PRD-2026-000011', 2, 4),
(12, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'All-day comfort sports shoes',   true, 'Puma Sports Shoes',              'PRD-2026-000012', 3, 4),
(13, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Genuine leather belt',           true, 'Levi''s Leather Belt',            'PRD-2026-000013', 4, 5),
(14, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Cotton twill trucker cap',       true, 'Allen Solly Trucker Cap',        'PRD-2026-000014', 5, 5),
(15, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'Performance running cap',        true, 'Nike Performance Cap',           'PRD-2026-000015', 1, 5);

-- ---------------------------------------------------------------------
-- PRODUCT VARIANTS (30)  -- 2 per product (size / colour)
-- ---------------------------------------------------------------------
INSERT INTO product_variants (id, created_at, created_by, is_active, updated_at, updated_by, version, barcode, minimum_selling_price, purchase_price, selling_price, sku, product_id) VALUES
(1,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000001',  650.00,  450.00,  799.00,  'SKU-2026-000001',  1),
(2,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000002',  650.00,  450.00,  799.00,  'SKU-2026-000002',  1),
(3,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000003', 1400.00,  950.00, 1699.00,  'SKU-2026-000003',  2),
(4,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000004', 1400.00,  950.00, 1699.00,  'SKU-2026-000004',  2),
(5,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000005',  900.00,  600.00, 1199.00,  'SKU-2026-000005',  3),
(6,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000006',  900.00,  600.00, 1199.00,  'SKU-2026-000006',  3),
(7,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000007', 1050.00,  700.00, 1399.00,  'SKU-2026-000007',  4),
(8,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000008', 1050.00,  700.00, 1399.00,  'SKU-2026-000008',  4),
(9,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000009',  800.00,  550.00, 1099.00,  'SKU-2026-000009',  5),
(10, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000010',  800.00,  550.00, 1099.00,  'SKU-2026-000010',  5),
(11, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000011',  750.00,  500.00,  999.00,  'SKU-2026-000011',  6),
(12, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000012',  750.00,  500.00,  999.00,  'SKU-2026-000012',  6),
(13, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000013',  450.00,  300.00,  599.00,  'SKU-2026-000013',  7),
(14, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000014',  450.00,  300.00,  599.00,  'SKU-2026-000014',  7),
(15, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000015',  400.00,  250.00,  549.00,  'SKU-2026-000015',  8),
(16, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000016',  400.00,  250.00,  549.00,  'SKU-2026-000016',  8),
(17, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000017',  650.00,  450.00,  899.00,  'SKU-2026-000017',  9),
(18, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000018',  650.00,  450.00,  899.00,  'SKU-2026-000018',  9),
(19, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000019', 2600.00, 1800.00, 3499.00,  'SKU-2026-000019', 10),
(20, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000020', 2600.00, 1800.00, 3499.00,  'SKU-2026-000020', 10),
(21, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000021', 2200.00, 1500.00, 2999.00,  'SKU-2026-000021', 11),
(22, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000022', 2200.00, 1500.00, 2999.00,  'SKU-2026-000022', 11),
(23, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000023', 1800.00, 1200.00, 2499.00,  'SKU-2026-000023', 12),
(24, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000024', 1800.00, 1200.00, 2499.00,  'SKU-2026-000024', 12),
(25, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000025',  550.00,  350.00,  799.00,  'SKU-2026-000025', 13),
(26, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000026',  550.00,  350.00,  799.00,  'SKU-2026-000026', 13),
(27, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000027',  300.00,  200.00,  449.00,  'SKU-2026-000027', 14),
(28, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000028',  300.00,  200.00,  449.00,  'SKU-2026-000028', 14),
(29, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000029',  400.00,  250.00,  549.00,  'SKU-2026-000029', 15),
(30, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 'BAR-2026-000030',  400.00,  250.00,  549.00,  'SKU-2026-000030', 15);

-- ---------------------------------------------------------------------
-- INVENTORY (30)  -- current = purchased - sold, matches movements
-- ---------------------------------------------------------------------
INSERT INTO inventories (id, created_at, created_by, is_active, updated_at, updated_by, version, current_stock, maximum_stock, minimum_stock, reorder_level, reserved_stock, product_variant_id) VALUES
(1,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 40, 100, 10, 15, 0,  1),
(2,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 35, 100, 10, 15, 0,  2),
(3,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 45, 100, 10, 15, 0,  3),
(4,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 30, 100, 10, 15, 0,  4),
(5,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 50, 120, 10, 15, 0,  5),
(6,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 40, 120, 10, 15, 0,  6),
(7,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 25, 100, 10, 15, 0,  7),
(8,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 30, 100, 10, 15, 0,  8),
(9,  now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 40, 120, 10, 15, 0,  9),
(10, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 35, 120, 10, 15, 0, 10),
(11, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 45, 120, 10, 15, 0, 11),
(12, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 30, 120, 10, 15, 0, 12),
(13, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 60, 150, 15, 20, 0, 13),
(14, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 50, 150, 15, 20, 0, 14),
(15, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 45, 150, 15, 20, 0, 15),
(16, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 40, 150, 15, 20, 0, 16),
(17, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 35, 120, 10, 15, 0, 17),
(18, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 30, 120, 10, 15, 0, 18),
(19, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 25, 60,  5,  8, 0, 19),
(20, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 20, 60,  5,  8, 0, 20),
(21, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 22, 60,  5,  8, 0, 21),
(22, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 18, 60,  5,  8, 0, 22),
(23, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 24, 60,  5,  8, 0, 23),
(24, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 20, 60,  5,  8, 0, 24),
(25, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 30, 100, 10, 15, 0, 25),
(26, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 28, 100, 10, 15, 0, 26),
(27, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 40, 120, 10, 15, 0, 27),
(28, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 35, 120, 10, 15, 0, 28),
(29, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 30, 120, 10, 15, 0, 29),
(30, now() - interval '100 days', 1, true, now() - interval '1 day', 1, 0, 25, 120, 10, 15, 0, 30);

-- ---------------------------------------------------------------------
-- PURCHASES (10)  -- all RECEIVED (stock has been added)
-- ---------------------------------------------------------------------
INSERT INTO purchases (id, created_at, created_by, is_active, updated_at, updated_by, version, purchase_date, purchase_number, remarks, status, total_amount, supplier_id) VALUES
(1,  now() - interval '95 days', 1, true, now() - interval '95 days', 1, 0, CURRENT_DATE - 95, 'PO-2026-000001', 'Opening stock - polo t-shirts & running shoes', 'RECEIVED',  35550.00, 1),
(2,  now() - interval '88 days', 1, true, now() - interval '88 days', 1, 0, CURRENT_DATE - 88, 'PO-2026-000002', 'Denim jeans restock', 'RECEIVED',  73150.00, 1),
(3,  now() - interval '80 days', 1, true, now() - interval '80 days', 1, 0, CURRENT_DATE - 80, 'PO-2026-000003', 'Track pants season order', 'RECEIVED',  55200.00, 2),
(4,  now() - interval '72 days', 1, true, now() - interval '72 days', 1, 0, CURRENT_DATE - 72, 'PO-2026-000004', 'Kurtas new arrival', 'RECEIVED',  40600.00, 2),
(5,  now() - interval '64 days', 1, true, now() - interval '64 days', 1, 0, CURRENT_DATE - 64, 'PO-2026-000005', 'Sports top & leggings', 'RECEIVED',  65350.00, 3),
(6,  now() - interval '56 days', 1, true, now() - interval '56 days', 1, 0, CURRENT_DATE - 56, 'PO-2026-000006', 'Kids wear batch', 'RECEIVED',  49400.00, 3),
(7,  now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, CURRENT_DATE - 48, 'PO-2026-000007', 'Kids shorts & hoodies', 'RECEIVED',  37950.00, 4),
(8,  now() - interval '40 days', 1, true, now() - interval '40 days', 1, 0, CURRENT_DATE - 40, 'PO-2026-000008', 'Footwear stock', 'RECEIVED',  98550.00, 4),
(9,  now() - interval '32 days', 1, true, now() - interval '32 days', 1, 0, CURRENT_DATE - 32, 'PO-2026-000009', 'Sneakers & sports shoes', 'RECEIVED', 118200.00, 5),
(10, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, CURRENT_DATE - 25, 'PO-2026-000010', 'Accessories restock', 'RECEIVED',  50650.00, 5);

-- ---------------------------------------------------------------------
-- PURCHASE ITEMS (30)
-- ---------------------------------------------------------------------
INSERT INTO purchase_items (id, created_at, created_by, is_active, updated_at, updated_by, version, product_variant_id, purchase_price, quantity, purchase_id) VALUES
(1,  now() - interval '95 days', 1, true, now() - interval '95 days', 1, 0,  1,  450.00,  41, 1),
(2,  now() - interval '95 days', 1, true, now() - interval '95 days', 1, 0,  2,  450.00,  38, 1),
(3,  now() - interval '88 days', 1, true, now() - interval '88 days', 1, 0,  3,  950.00,  46, 2),
(4,  now() - interval '88 days', 1, true, now() - interval '88 days', 1, 0,  4,  950.00,  31, 2),
(5,  now() - interval '80 days', 1, true, now() - interval '80 days', 1, 0,  5,  600.00,  51, 3),
(6,  now() - interval '80 days', 1, true, now() - interval '80 days', 1, 0,  6,  600.00,  41, 3),
(7,  now() - interval '72 days', 1, true, now() - interval '72 days', 1, 0,  7,  700.00,  27, 4),
(8,  now() - interval '72 days', 1, true, now() - interval '72 days', 1, 0,  8,  700.00,  31, 4),
(9,  now() - interval '64 days', 1, true, now() - interval '64 days', 1, 0,  9,  550.00,  41, 5),
(10, now() - interval '64 days', 1, true, now() - interval '64 days', 1, 0, 10,  550.00,  36, 5),
(11, now() - interval '64 days', 1, true, now() - interval '64 days', 1, 0, 11,  500.00,  46, 5),
(12, now() - interval '56 days', 1, true, now() - interval '56 days', 1, 0, 12,  500.00,  31, 6),
(13, now() - interval '56 days', 1, true, now() - interval '56 days', 1, 0, 13,  300.00,  62, 6),
(14, now() - interval '56 days', 1, true, now() - interval '56 days', 1, 0, 14,  300.00,  51, 6),
(15, now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, 15,  250.00,  46, 7),
(16, now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, 16,  250.00,  41, 7),
(17, now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, 17,  450.00,  36, 7),
(18, now() - interval '40 days', 1, true, now() - interval '40 days', 1, 0, 18,  450.00,  31, 8),
(19, now() - interval '40 days', 1, true, now() - interval '40 days', 1, 0, 19, 1800.00,  26, 8),
(20, now() - interval '40 days', 1, true, now() - interval '40 days', 1, 0, 20, 1800.00,  21, 8),
(21, now() - interval '32 days', 1, true, now() - interval '32 days', 1, 0, 21, 1500.00,  23, 9),
(22, now() - interval '32 days', 1, true, now() - interval '32 days', 1, 0, 22, 1500.00,  19, 9),
(23, now() - interval '32 days', 1, true, now() - interval '32 days', 1, 0, 23, 1200.00,  25, 9),
(24, now() - interval '32 days', 1, true, now() - interval '32 days', 1, 0, 24, 1200.00,  21, 9),
(25, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, 25,  350.00,  31, 10),
(26, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, 26,  350.00,  29, 10),
(27, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, 27,  200.00,  41, 10),
(28, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, 28,  200.00,  36, 10),
(29, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, 29,  250.00,  31, 10),
(30, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, 30,  250.00,  26, 10);

-- ---------------------------------------------------------------------
-- STOCK MOVEMENTS - PURCHASE RECEIPTS (IN)
-- ---------------------------------------------------------------------
INSERT INTO stock_movements (id, created_at, created_by, is_active, updated_at, updated_by, version, adjustment_type, movement_type, quantity, reference_number, reference_type, remarks, inventory_id) VALUES
(1,  now() - interval '95 days', 1, true, now() - interval '95 days', 1, 0, NULL, 'PURCHASE',  41, 'PO-2026-000001', 'PURCHASE_ORDER', 'Purchase received', 1),
(2,  now() - interval '95 days', 1, true, now() - interval '95 days', 1, 0, NULL, 'PURCHASE',  38, 'PO-2026-000001', 'PURCHASE_ORDER', 'Purchase received', 2),
(3,  now() - interval '88 days', 1, true, now() - interval '88 days', 1, 0, NULL, 'PURCHASE',  46, 'PO-2026-000002', 'PURCHASE_ORDER', 'Purchase received', 3),
(4,  now() - interval '88 days', 1, true, now() - interval '88 days', 1, 0, NULL, 'PURCHASE',  31, 'PO-2026-000002', 'PURCHASE_ORDER', 'Purchase received', 4),
(5,  now() - interval '80 days', 1, true, now() - interval '80 days', 1, 0, NULL, 'PURCHASE',  51, 'PO-2026-000003', 'PURCHASE_ORDER', 'Purchase received', 5),
(6,  now() - interval '80 days', 1, true, now() - interval '80 days', 1, 0, NULL, 'PURCHASE',  41, 'PO-2026-000003', 'PURCHASE_ORDER', 'Purchase received', 6),
(7,  now() - interval '72 days', 1, true, now() - interval '72 days', 1, 0, NULL, 'PURCHASE',  27, 'PO-2026-000004', 'PURCHASE_ORDER', 'Purchase received', 7),
(8,  now() - interval '72 days', 1, true, now() - interval '72 days', 1, 0, NULL, 'PURCHASE',  31, 'PO-2026-000004', 'PURCHASE_ORDER', 'Purchase received', 8),
(9,  now() - interval '64 days', 1, true, now() - interval '64 days', 1, 0, NULL, 'PURCHASE',  41, 'PO-2026-000005', 'PURCHASE_ORDER', 'Purchase received', 9),
(10, now() - interval '64 days', 1, true, now() - interval '64 days', 1, 0, NULL, 'PURCHASE',  36, 'PO-2026-000005', 'PURCHASE_ORDER', 'Purchase received', 10),
(11, now() - interval '64 days', 1, true, now() - interval '64 days', 1, 0, NULL, 'PURCHASE',  46, 'PO-2026-000005', 'PURCHASE_ORDER', 'Purchase received', 11),
(12, now() - interval '56 days', 1, true, now() - interval '56 days', 1, 0, NULL, 'PURCHASE',  31, 'PO-2026-000006', 'PURCHASE_ORDER', 'Purchase received', 12),
(13, now() - interval '56 days', 1, true, now() - interval '56 days', 1, 0, NULL, 'PURCHASE',  62, 'PO-2026-000006', 'PURCHASE_ORDER', 'Purchase received', 13),
(14, now() - interval '56 days', 1, true, now() - interval '56 days', 1, 0, NULL, 'PURCHASE',  51, 'PO-2026-000006', 'PURCHASE_ORDER', 'Purchase received', 14),
(15, now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, NULL, 'PURCHASE',  46, 'PO-2026-000007', 'PURCHASE_ORDER', 'Purchase received', 15),
(16, now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, NULL, 'PURCHASE',  41, 'PO-2026-000007', 'PURCHASE_ORDER', 'Purchase received', 16),
(17, now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, NULL, 'PURCHASE',  36, 'PO-2026-000007', 'PURCHASE_ORDER', 'Purchase received', 17),
(18, now() - interval '40 days', 1, true, now() - interval '40 days', 1, 0, NULL, 'PURCHASE',  31, 'PO-2026-000008', 'PURCHASE_ORDER', 'Purchase received', 18),
(19, now() - interval '40 days', 1, true, now() - interval '40 days', 1, 0, NULL, 'PURCHASE',  26, 'PO-2026-000008', 'PURCHASE_ORDER', 'Purchase received', 19),
(20, now() - interval '40 days', 1, true, now() - interval '40 days', 1, 0, NULL, 'PURCHASE',  21, 'PO-2026-000008', 'PURCHASE_ORDER', 'Purchase received', 20),
(21, now() - interval '32 days', 1, true, now() - interval '32 days', 1, 0, NULL, 'PURCHASE',  23, 'PO-2026-000009', 'PURCHASE_ORDER', 'Purchase received', 21),
(22, now() - interval '32 days', 1, true, now() - interval '32 days', 1, 0, NULL, 'PURCHASE',  19, 'PO-2026-000009', 'PURCHASE_ORDER', 'Purchase received', 22),
(23, now() - interval '32 days', 1, true, now() - interval '32 days', 1, 0, NULL, 'PURCHASE',  25, 'PO-2026-000009', 'PURCHASE_ORDER', 'Purchase received', 23),
(24, now() - interval '32 days', 1, true, now() - interval '32 days', 1, 0, NULL, 'PURCHASE',  21, 'PO-2026-000009', 'PURCHASE_ORDER', 'Purchase received', 24),
(25, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, NULL, 'PURCHASE',  31, 'PO-2026-000010', 'PURCHASE_ORDER', 'Purchase received', 25),
(26, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, NULL, 'PURCHASE',  29, 'PO-2026-000010', 'PURCHASE_ORDER', 'Purchase received', 26),
(27, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, NULL, 'PURCHASE',  41, 'PO-2026-000010', 'PURCHASE_ORDER', 'Purchase received', 27),
(28, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, NULL, 'PURCHASE',  36, 'PO-2026-000010', 'PURCHASE_ORDER', 'Purchase received', 28),
(29, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, NULL, 'PURCHASE',  31, 'PO-2026-000010', 'PURCHASE_ORDER', 'Purchase received', 29),
(30, now() - interval '25 days', 1, true, now() - interval '25 days', 1, 0, NULL, 'PURCHASE',  26, 'PO-2026-000010', 'PURCHASE_ORDER', 'Purchase received', 30);

-- ---------------------------------------------------------------------
-- STOCK MOVEMENTS - SALES (OUT)
-- ---------------------------------------------------------------------
INSERT INTO stock_movements (id, created_at, created_by, is_active, updated_at, updated_by, version, adjustment_type, movement_type, quantity, reference_number, reference_type, remarks, inventory_id) VALUES
(31, now() - interval '65 days', 1, true, now() - interval '65 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000001', 'SALES_INVOICE', 'Sale completed', 2),
(32, now() - interval '65 days', 1, true, now() - interval '65 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000001', 'SALES_INVOICE', 'Sale completed', 19),
(33, now() - interval '65 days', 1, true, now() - interval '65 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000001', 'SALES_INVOICE', 'Sale completed', 25),
(34, now() - interval '62 days', 1, true, now() - interval '62 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000002', 'SALES_INVOICE', 'Sale completed', 3),
(35, now() - interval '62 days', 1, true, now() - interval '62 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000002', 'SALES_INVOICE', 'Sale completed', 27),
(36, now() - interval '58 days', 1, true, now() - interval '58 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000003', 'SALES_INVOICE', 'Sale completed', 5),
(37, now() - interval '58 days', 1, true, now() - interval '58 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000003', 'SALES_INVOICE', 'Sale completed', 11),
(38, now() - interval '55 days', 1, true, now() - interval '55 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000004', 'SALES_INVOICE', 'Sale completed', 7),
(39, now() - interval '55 days', 1, true, now() - interval '55 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000004', 'SALES_INVOICE', 'Sale completed', 29),
(40, now() - interval '52 days', 1, true, now() - interval '52 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000005', 'SALES_INVOICE', 'Sale completed', 21),
(41, now() - interval '52 days', 1, true, now() - interval '52 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000005', 'SALES_INVOICE', 'Sale completed', 1),
(42, now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000006', 'SALES_INVOICE', 'Sale completed', 9),
(43, now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000006', 'SALES_INVOICE', 'Sale completed', 13),
(44, now() - interval '48 days', 1, true, now() - interval '48 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000006', 'SALES_INVOICE', 'Sale completed', 15),
(45, now() - interval '44 days', 1, true, now() - interval '44 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000007', 'SALES_INVOICE', 'Sale completed', 23),
(46, now() - interval '44 days', 1, true, now() - interval '44 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000007', 'SALES_INVOICE', 'Sale completed', 28),
(47, now() - interval '40 days', 1, true, now() - interval '40 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000008', 'SALES_INVOICE', 'Sale completed', 4),
(48, now() - interval '40 days', 1, true, now() - interval '40 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000008', 'SALES_INVOICE', 'Sale completed', 26),
(49, now() - interval '35 days', 1, true, now() - interval '35 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000009', 'SALES_INVOICE', 'Sale completed', 10),
(50, now() - interval '35 days', 1, true, now() - interval '35 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000009', 'SALES_INVOICE', 'Sale completed', 14),
(51, now() - interval '31 days', 1, true, now() - interval '31 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000010', 'SALES_INVOICE', 'Sale completed', 6),
(52, now() - interval '31 days', 1, true, now() - interval '31 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000010', 'SALES_INVOICE', 'Sale completed', 12),
(53, now() - interval '31 days', 1, true, now() - interval '31 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000010', 'SALES_INVOICE', 'Sale completed', 20),
(54, now() - interval '27 days', 1, true, now() - interval '27 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000011', 'SALES_INVOICE', 'Sale completed', 17),
(55, now() - interval '27 days', 1, true, now() - interval '27 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000011', 'SALES_INVOICE', 'Sale completed', 24),
(56, now() - interval '22 days', 1, true, now() - interval '22 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000012', 'SALES_INVOICE', 'Sale completed', 8),
(57, now() - interval '22 days', 1, true, now() - interval '22 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000012', 'SALES_INVOICE', 'Sale completed', 16),
(58, now() - interval '17 days', 1, true, now() - interval '17 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000013', 'SALES_INVOICE', 'Sale completed', 18),
(59, now() - interval '17 days', 1, true, now() - interval '17 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000013', 'SALES_INVOICE', 'Sale completed', 22),
(60, now() - interval '12 days', 1, true, now() - interval '12 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000014', 'SALES_INVOICE', 'Sale completed', 30),
(61, now() - interval '12 days', 1, true, now() - interval '12 days', 1, 0, NULL, 'SALE', 1, 'SAL-2026-000014', 'SALES_INVOICE', 'Sale completed', 13),
(62, now() - interval '7 days',  1, true, now() - interval '7 days',  1, 0, NULL, 'SALE', 2, 'SAL-2026-000015', 'SALES_INVOICE', 'Sale completed', 2),
(63, now() - interval '7 days',  1, true, now() - interval '7 days',  1, 0, NULL, 'SALE', 1, 'SAL-2026-000015', 'SALES_INVOICE', 'Sale completed', 7);

COMMIT;
