-- Flyway migration: expose + persist allow_partial_payment for invoice settings.
-- Partial payment BEHAVIOR is not implemented yet; the flag is stored so the
-- frontend settings contract can round-trip it safely.
ALTER TABLE invoice_settings
    ADD COLUMN allow_partial_payment boolean NOT NULL DEFAULT FALSE;