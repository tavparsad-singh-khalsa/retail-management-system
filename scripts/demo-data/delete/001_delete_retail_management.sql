-- =====================================================================
-- Delete ALL business / demo / stress-test data from retail_management
-- Keeps: roles, users, Flyway metadata, and empty system tables.
-- FK-safe order: children before parents.
-- =====================================================================

DELETE FROM stock_movements;
DELETE FROM inventories;
DELETE FROM product_images;
DELETE FROM variant_attributes;
DELETE FROM purchase_items;
DELETE FROM purchases;
DELETE FROM product_variants;
DELETE FROM products;
DELETE FROM suppliers;
DELETE FROM categories;
DELETE FROM brands;
DELETE FROM attribute_values;
DELETE FROM attributes;
