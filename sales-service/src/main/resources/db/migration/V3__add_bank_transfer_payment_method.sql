-- The V1 payments CHECK constraint omitted BANK_TRANSFER even though the sales
-- PaymentMethod enum, billing PaymentMethod enum, and the mobile POS all accept
-- it. Rebuild the constraint to include BANK_TRANSFER so a bank-transfer
-- settlement no longer violates the database-level check.
ALTER TABLE payments DROP CONSTRAINT payments_payment_method_check;
ALTER TABLE payments ADD CONSTRAINT payments_payment_method_check
    CHECK (payment_method IN ('CASH','CARD','UPI','BANK_TRANSFER','NET_BANKING','WALLET','CHEQUE','OTHER'));