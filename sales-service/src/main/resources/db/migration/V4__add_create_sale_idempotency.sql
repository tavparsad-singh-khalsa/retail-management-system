-- STEP 19B: create-sale idempotency.
-- Idempotency-Key (client-supplied) + deterministic request fingerprint.
-- Both columns are nullable so legacy rows and header-less requests keep working.
ALTER TABLE sales ADD COLUMN idempotency_key VARCHAR(64);
ALTER TABLE sales ADD COLUMN idempotency_request_hash VARCHAR(64);
CREATE UNIQUE INDEX uk_sales_idempotency_key ON sales (idempotency_key);