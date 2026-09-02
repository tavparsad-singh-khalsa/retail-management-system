-- Flyway migration: invoice settings (single-row config, id = 1)
CREATE TABLE invoice_settings (
    id bigint PRIMARY KEY,
    invoice_prefix varchar(10) NOT NULL DEFAULT 'INV',
    default_currency varchar(3) NOT NULL DEFAULT 'INR',
    tax_rate numeric(5,2) NOT NULL DEFAULT 0.00,
    payment_terms_days integer NOT NULL DEFAULT 30,
    email_notifications_enabled boolean NOT NULL DEFAULT TRUE,
    sms_notifications_enabled boolean NOT NULL DEFAULT TRUE,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO invoice_settings (id) VALUES (1);