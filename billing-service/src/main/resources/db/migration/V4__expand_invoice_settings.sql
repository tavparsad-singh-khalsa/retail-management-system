-- Flyway migration: expand invoice_settings to shop info, branding, display toggles, tax config.
-- Non-destructive: renames preserve existing values; new columns get defaults; retains row id = 1.

ALTER TABLE invoice_settings
    RENAME COLUMN default_currency TO currency;

ALTER TABLE invoice_settings
    RENAME COLUMN payment_terms_days TO payment_terms;

ALTER TABLE invoice_settings
    DROP COLUMN email_notifications_enabled;

ALTER TABLE invoice_settings
    DROP COLUMN sms_notifications_enabled;

ALTER TABLE invoice_settings
    ADD COLUMN shop_name varchar(255) NOT NULL DEFAULT '',
    ADD COLUMN address varchar(500) NOT NULL DEFAULT '',
    ADD COLUMN phone_number varchar(50) NOT NULL DEFAULT '',
    ADD COLUMN email varchar(255),
    ADD COLUMN instagram_url varchar(255),
    ADD COLUMN google_maps_url varchar(500),
    ADD COLUMN google_review_url varchar(500),
    ADD COLUMN invoice_tagline varchar(255) NOT NULL DEFAULT '',
    ADD COLUMN footer_text varchar(500) NOT NULL DEFAULT '',
    ADD COLUMN show_shop_name boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN show_address boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN show_phone boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN show_email boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN show_instagram boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN show_google_maps boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN show_google_review boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN show_tagline boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN show_payment_terms boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN show_footer boolean NOT NULL DEFAULT TRUE,
    ADD COLUMN tax_enabled boolean NOT NULL DEFAULT FALSE,
    ADD COLUMN tax_name varchar(50) NOT NULL DEFAULT 'GST',
    ADD COLUMN show_tax boolean NOT NULL DEFAULT FALSE,
    ADD COLUMN gstin varchar(30),
    ADD COLUMN show_gstin boolean NOT NULL DEFAULT FALSE;