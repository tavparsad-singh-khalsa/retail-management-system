SELECT setval(pg_get_serial_sequence('invoices', 'id'),      COALESCE((SELECT MAX(id) FROM invoices),      1));
SELECT setval(pg_get_serial_sequence('invoice_items', 'id'), COALESCE((SELECT MAX(id) FROM invoice_items), 1));
