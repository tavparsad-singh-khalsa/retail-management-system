SELECT setval(pg_get_serial_sequence('sales', 'id'),       COALESCE((SELECT MAX(id) FROM sales),       1));
SELECT setval(pg_get_serial_sequence('sale_items', 'id'),  COALESCE((SELECT MAX(id) FROM sale_items),  1));
SELECT setval(pg_get_serial_sequence('payments', 'id'),    COALESCE((SELECT MAX(id) FROM payments),    1));
