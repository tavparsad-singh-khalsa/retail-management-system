-- STEP 23 / P2-02: Enforce RETURN (sale cancellation) idempotency.
--
-- cancelSale must generate a deterministic reference per (sale, variant) so
-- retries produce the same identity. The StockMovementServiceImpl pre-check
-- replays an existing RETURN with the same reference without mutating stock.
-- The unique index below makes that replay race-safe: two concurrent restores
-- with the same (inventory, reference) for RETURN cannot both create movements.
--
-- Scope is deliberately RETURN-only, mirroring the SALE-only index in V2.
-- PURCHASE multi-delivery orders, ADJUSTMENT, DAMAGE, RESERVE/UNRESERVE keep
-- their existing reference semantics (they may reuse references legitimately).
--
-- Guard fails loudly if duplicate RETURN rows already exist; never deletes data.

DO $$
DECLARE
    duplicate_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO duplicate_count
    FROM (
        SELECT inventory_id, reference_number
        FROM stock_movements
        WHERE movement_type = 'RETURN' AND reference_number IS NOT NULL
        GROUP BY inventory_id, reference_number
        HAVING COUNT(*) > 1
    ) d;

    IF duplicate_count > 0 THEN
        RAISE EXCEPTION
            'Cannot enforce RETURN reference uniqueness: found % duplicate (inventory_id, reference_number) RETURN stock movements. Review the data before applying this migration.',
            duplicate_count;
    END IF;
END $$;

CREATE UNIQUE INDEX uk_stock_movements_return_reference
    ON stock_movements (inventory_id, reference_number)
    WHERE movement_type = 'RETURN';
