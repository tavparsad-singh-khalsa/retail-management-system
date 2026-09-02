SELECT setval('notifications_id_seq', COALESCE((SELECT MAX(id) FROM notifications), 1));
SELECT setval('report_logs_id_seq',   COALESCE((SELECT MAX(id) FROM report_logs),   1));
