-- Add product snapshot columns to sale_items so invoice PDFs can carry
-- product names/SKUs when billing-service builds invoice items from a sale.
ALTER TABLE sale_items
    ADD COLUMN product_id BIGINT,
    ADD COLUMN sku VARCHAR(100),
    ADD COLUMN barcode VARCHAR(128),
    ADD COLUMN product_name VARCHAR(255);