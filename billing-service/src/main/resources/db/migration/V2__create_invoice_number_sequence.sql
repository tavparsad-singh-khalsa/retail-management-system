CREATE SEQUENCE invoice_number_seq;

SELECT setval(
    'invoice_number_seq',
    COALESCE((SELECT MAX(id) FROM invoices), 0) + 1,
    false
);
