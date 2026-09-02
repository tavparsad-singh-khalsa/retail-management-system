-- retail_report (report-service) - sample report logs
BEGIN;

INSERT INTO report_logs (id, report_number, report_type, start_date, end_date, generated_by, status, report_url, execution_time_ms, generated_at, updated_at, remarks) VALUES
(1, 'REP-2026-000001', 'SALES_SUMMARY',      DATE '2026-06-01', DATE '2026-06-30', 'owner', 'SUCCESS', '/reports/REP-2026-000001.json',  42, TIMESTAMP '2026-07-01 09:00:00', TIMESTAMP '2026-07-01 09:00:00', 'Successfully generated Sales Report. Total revenue: 19429'),
(2, 'REP-2026-000002', 'PURCHASE_SUMMARY',   DATE '2026-06-01', DATE '2026-06-30', 'owner', 'SUCCESS', '/reports/REP-2026-000002.json',  38, TIMESTAMP '2026-07-01 09:05:00', TIMESTAMP '2026-07-01 09:05:00', 'Successfully generated Purchase Report. Total cost: 469900'),
(3, 'REP-2026-000003', 'PROFIT_LOSS',        DATE '2026-06-01', DATE '2026-06-30', 'owner', 'SUCCESS', '/reports/REP-2026-000003.json',  51, TIMESTAMP '2026-07-01 09:10:00', TIMESTAMP '2026-07-01 09:10:00', 'Successfully generated Profit & Loss Report. Gross profit: -450471'),
(4, 'REP-2026-000004', 'INVENTORY_STATUS',   NULL,             NULL,             'owner', 'SUCCESS', '/reports/REP-2026-000004.json',  27, TIMESTAMP '2026-07-01 09:15:00', TIMESTAMP '2026-07-01 09:15:00', 'Successfully generated Inventory Status Report. Low/Out of stock items: 0'),
(5, 'REP-2026-000005', 'SALES_SUMMARY',      DATE '2026-07-01', DATE '2026-07-31', 'owner', 'SUCCESS', '/reports/REP-2026-000005.json',  33, TIMESTAMP '2026-08-01 09:00:00', TIMESTAMP '2026-08-01 09:00:00', 'Successfully generated Sales Report. Total revenue: 24237'),
(6, 'REP-2026-000006', 'PURCHASE_SUMMARY',   DATE '2026-07-01', DATE '2026-07-31', 'owner', 'SUCCESS', '/reports/REP-2026-000006.json',  29, TIMESTAMP '2026-08-01 09:05:00', TIMESTAMP '2026-08-01 09:05:00', 'Successfully generated Purchase Report. Total cost: 154700'),
(7, 'REP-2026-000007', 'EXECUTIVE_DASHBOARD',NULL,             NULL,             'owner', 'SUCCESS', '/reports/REP-2026-000007.json',  65, TIMESTAMP '2026-08-06 08:00:00', TIMESTAMP '2026-08-06 08:00:00', 'Successfully generated Executive Dashboard.'),
(8, 'REP-2026-000008', 'PROFIT_LOSS',        DATE '2026-06-01', DATE '2026-07-31', 'owner', 'SUCCESS', '/reports/REP-2026-000008.json',  57, TIMESTAMP '2026-08-06 08:05:00', TIMESTAMP '2026-08-06 08:05:00', 'Successfully generated Profit & Loss Report.');

COMMIT;
