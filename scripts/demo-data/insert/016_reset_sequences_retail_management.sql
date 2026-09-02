-- Reset identity sequences to MAX(id) so new inserts do not collide
SELECT setval(pg_get_serial_sequence('categories', 'id'),       COALESCE((SELECT MAX(id) FROM categories),       1));
SELECT setval(pg_get_serial_sequence('brands', 'id'),           COALESCE((SELECT MAX(id) FROM brands),           1));
SELECT setval(pg_get_serial_sequence('suppliers', 'id'),        COALESCE((SELECT MAX(id) FROM suppliers),        1));
SELECT setval(pg_get_serial_sequence('products', 'id'),         COALESCE((SELECT MAX(id) FROM products),         1));
SELECT setval(pg_get_serial_sequence('product_variants', 'id'), COALESCE((SELECT MAX(id) FROM product_variants), 1));
SELECT setval(pg_get_serial_sequence('inventories', 'id'),      COALESCE((SELECT MAX(id) FROM inventories),      1));
SELECT setval(pg_get_serial_sequence('purchases', 'id'),        COALESCE((SELECT MAX(id) FROM purchases),        1));
SELECT setval(pg_get_serial_sequence('purchase_items', 'id'),   COALESCE((SELECT MAX(id) FROM purchase_items),   1));
SELECT setval(pg_get_serial_sequence('stock_movements', 'id'),  COALESCE((SELECT MAX(id) FROM stock_movements),  1));
SELECT setval(pg_get_serial_sequence('attributes', 'id'),       COALESCE((SELECT MAX(id) FROM attributes),       1));
SELECT setval(pg_get_serial_sequence('attribute_values', 'id'), COALESCE((SELECT MAX(id) FROM attribute_values), 1));
SELECT setval(pg_get_serial_sequence('product_images', 'id'),   COALESCE((SELECT MAX(id) FROM product_images),   1));
SELECT setval(pg_get_serial_sequence('variant_attributes', 'id'), COALESCE((SELECT MAX(id) FROM variant_attributes), 1));
