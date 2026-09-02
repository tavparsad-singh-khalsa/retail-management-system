-- STEP 19D: Enforce SALE inventory-deduction idempotency.
--
-- A deduction request that repeats an already-processed operation must not
-- deduct stock twice. Uniqueness is deliberately scoped to SALE movements:
-- at most one SALE movement per (inventory, reference number). This keeps the
-- reference unique per (sale operation, variant) while leaving other movement
-- types untouched (multi-delivery PURCHASE orders, RETURN, ADJUSTMENT, RESERVE
-- and DAMAGE keep their existing reference semantics).
--
-- The guard below fails loudly if any existing data already violates the rule
-- (e.g. duplicate SALE rows recorded before this index). It never deletes or
-- rewrites data; on violation the migration must be reviewed before proceeding.

DO $$
DECLARE
    duplicate_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO duplicate_count
    FROM (
        SELECT inventory_id, reference_number
        FROM stock_movements
        WHERE movement_type = 'SALE' AND reference_number IS NOT NULL
        GROUP BY inventory_id, reference_number
        HAVING COUNT(*) > 1
    ) d;

    IF duplicate_count > 0 THEN
        RAISE EXCEPTION
            'Cannot enforce SALE reference uniqueness: found % duplicate (inventory_id, reference_number) SALE stock movements. Review the data before applying this migration.',
            duplicate_count;
    END IF;
END $$;

CREATE UNIQUE INDEX uk_stock_movements_sale_reference
    ON stock_movements (inventory_id, reference_number)
    WHERE movement_type = 'SALE';