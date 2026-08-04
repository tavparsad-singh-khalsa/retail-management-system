CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    notification_number VARCHAR(30) NOT NULL UNIQUE,
    notification_type VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    reference_type VARCHAR(30),
    reference_id BIGINT,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_notification_number ON notifications(notification_number);
CREATE INDEX idx_notification_recipient ON notifications(recipient);
CREATE INDEX idx_notification_reference ON notifications(reference_type, reference_id);
