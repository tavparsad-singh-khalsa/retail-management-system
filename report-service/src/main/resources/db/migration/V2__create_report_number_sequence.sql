CREATE SEQUENCE report_number_seq;

SELECT setval(
    'report_number_seq',
    COALESCE((SELECT MAX(id) FROM report_logs), 0) + 1,
    false
);
