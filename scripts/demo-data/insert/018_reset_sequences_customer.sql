SELECT setval(pg_get_serial_sequence('customers', 'id'),            COALESCE((SELECT MAX(id) FROM customers),            1));
SELECT setval(pg_get_serial_sequence('customer_addresses', 'id'),   COALESCE((SELECT MAX(id) FROM customer_addresses),   1));
