# Retail Management System — Stress Test Report

**Date:** 2026-08-05
**Method:** 100% REST API (no direct DB inserts), resumable PowerShell harness
**Stack:** 8 microservices (auth, product, purchase, sales, customer, billing, notification, report) + PostgreSQL, Docker Compose

---

## 1. Data Volume Achieved

| Entity          | Baseline | Target | Created | Final | Source DB          |
|-----------------|----------|--------|---------|-------|--------------------|
| Customers       | 52       | 500    | 448     | 500   | Customer_db        |
| Suppliers       | 7        | 200    | 193     | 200   | retail_management  |
| Products        | 51       | 1000   | 949     | 1000  | retail_management  |
| Product variants| 101      | 5000   | 4899    | 5000  | retail_management  |
| Inventories     | 101      | 5000   | —       | 5000  | retail_management  |
| Purchases (POs) | 4        | 2000   | 1996    | 2000  | retail_management  |
| Purchase items  | —        | —      | —       | 5992  | retail_management  |
| Stock movements | —        | —      | —       | 8992  | retail_management  |
| Sales           | 4        | 3000   | 2996    | 3000  | sales_db           |
| Payments        | 4        | 3000   | —       | 3000  | sales_db           |
| Invoices        | 4        | 3000   | 2996    | 3000  | retail_billing     |
| Notifications   | 7        | 3000   | 2993    | 3000  | retail_notification|

All targets reached with **zero** creation failures across ~21,000 API calls.

---

## 2. Write Performance (per-op latency, single-threaded)

| Operation          | Calls | Avg    | Max    |
|--------------------|-------|--------|--------|
| create_variant     | 4899  | 8.4 ms | 114 ms |
| create_supplier    | 193   | 14.2 ms| 49 ms  |
| create_customer    | 448   | 11.9 ms| 36 ms  |
| create_purchase    | 1996  | 8.4 ms | 72 ms  |
| approve_purchase   | 1996  | 6.3 ms | 76 ms  |
| receive_purchase   | 1996  | 18.8 ms| 148 ms |
| create_sale        | 2996  | 18.7 ms| 171 ms |
| create_invoice     | 2996  | 16.1 ms| 83 ms  |
| create_notification| 2993  | 6.4 ms | 46 ms  |

Writes are fast and stable; max latencies under 175 ms even at the tail.

---

## 3. Read Performance

### Single sequential request (10 iterations avg/max)

| Endpoint        | Avg      | Max     | Payload |
|-----------------|----------|---------|---------|
| products        | 238 ms   | 303 ms  | ~?      |
| customers       | 259 ms   | 274 ms  | ~?      |
| notifications   | 424 ms   | 436 ms  | ~?      |
| purchases       | 1,839 ms | 1,899 ms| 874 KB  |
| invoices        | 1,991 ms | 2,040 ms| 1.6 MB  |
| inventory       | 2,223 ms | 2,426 ms| 1.2 MB  |
| variants        | 2,421 ms | 2,465 ms| 1.7 MB  |
| sales           | 3,198 ms | 3,418 ms| 1.6 MB  |

### 8-way concurrent burst (40 iterations each)

| Endpoint        | Avg       | Max     |
|-----------------|-----------|---------|
| notifications   | 112 ms    | 582 ms  |
| products        | 83 ms     | 691 ms  |
| customers       | 251 ms    | 780 ms  |
| invoices        | 1,406 ms  | 4,320 ms|
| purchases       | 1,992 ms  | 3,911 ms|
| inventory       | 2,155 ms  | 4,064 ms|
| variants        | 2,195 ms  | 4,026 ms|
| sales           | 2,937 ms  | 6,195 ms|

**Observation:** Large list endpoints (sales, variants, inventory, purchases, invoices)
return full result sets without pagination. At 1.6–1.7 MB payloads and 3,000–5,000 rows
this becomes the dominant cost. Not a correctness defect, but a scalability limiter.
Recommendation: add pagination / projection for list endpoints.

---

## 4. Report Generation Latency

| Report              | Time   |
|---------------------|--------|
| PURCHASE_SUMMARY    | 1,214 ms |
| INVENTORY_STATUS    | 1,380 ms |
| SALES_SUMMARY       | 2,087 ms |
| PROFIT_LOSS         | 3,192 ms |
| EXECUTIVE_DASHBOARD | 4,334 ms |

All report types generate successfully at full scale. Report logs: 20 records, all SUCCESS.

---

## 5. Integrity Verification

| Check | Result |
|-------|--------|
| Duplicate invoice_number | **0** |
| Duplicate notification_number | **0** |
| Duplicate sale_id in invoices (1 invoice/sale) | **0** |
| Duplicate purchase_number | **0** |
| Duplicate customer_code | **0** |
| Duplicate SKU | **0** |
| Duplicate sale_number | **0** |
| Inventory current_stock vs Σ movements | **5000/5000 match** |
| Negative / reserved-overflow stock | **0** |
| Purchase status | 2000/2000 RECEIVED |
| Sale status | 3000/3000 COMPLETED |
| Sale payment == sale total | **0 mismatches** |
| Invoice total == sale total | **0 mismatches** (3000 checked) |
| Invoice→sale orphan references | **0** |
| Invoice status distribution | 2997 GENERATED, 3 PAID |
| Notification status distribution | 2995 PENDING, 4 SENT, 1 FAILED |
| SQL exceptions in service logs | **0** across all 8 services |

---

## 6. Resource Usage (post-load)

| Container            | Memory   | Mem% | CPU% |
|----------------------|----------|------|------|
| report-service       | 422.3 MiB| 5.4% | 0.21%|
| billing-service      | 593.8 MiB| 7.6% | 0.21%|
| sales-service        | 572.6 MiB| 7.3% | 0.13%|
| product-service      | 659.3 MiB| 8.4% | 0.10%|
| notification-service | 478.8 MiB| 6.1% | 0.14%|
| customer-service     | 475.7 MiB| 6.1% | 0.14%|
| purchase-service     | 548.8 MiB| 7.0% | 0.11%|
| auth-service         | 425.5 MiB| 5.4% | 0.14%|
| postgres             | 195.6 MiB| 2.5% | 0.02%|

All containers well within the 7.664 GiB limit. Largest memory deltas vs baseline:
product +168 MiB, billing +152 MiB, sales +125 MiB, purchase +120 MiB — consistent with
in-memory aggregation of the added data.

---

## 7. Conclusion

- **No functional bugs surfaced** under load: no duplicate business numbers, no inventory
  drift, no orphaned records, no SQL errors, no negative stock, no creation failures.
- **Writes scale well** (sub-20 ms avg per write).
- **Known limitation:** unpaginated list endpoints produce 1.6–1.7 MB responses and 2–3 s
  read latency at this data volume; dashboard aggregation takes ~4.3 s. Recommend
  pagination/projection for reads. (Out of scope for this test — no architectural changes.)
- The earlier inventory-status-report bug (fixed during this session) remains fixed and
  verified at 100% scale: 5000 inventory records, 0 mismatches.
