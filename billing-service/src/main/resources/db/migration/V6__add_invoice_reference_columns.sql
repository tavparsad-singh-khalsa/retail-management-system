-- Flyway migration: add invoice reference snapshot columns.
-- Sales number and customer name are snapshotted at invoice creation time so
-- list/detail reads never need N+1 calls back to the sales/customer services.
-- This mirrors the STEP 8 product-identity snapshot pattern on sale_items.
ALTER TABLE invoices
    ADD COLUMN sale_number varchar(50),
    ADD COLUMN customer_name varchar(255);