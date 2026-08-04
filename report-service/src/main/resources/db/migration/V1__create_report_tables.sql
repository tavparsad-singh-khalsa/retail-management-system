CREATE TABLE report_logs (
    id BIGSERIAL PRIMARY KEY,
    report_number VARCHAR(50) NOT NULL UNIQUE,
    report_type VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    generated_by VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    report_url VARCHAR(255),
    execution_time_ms BIGINT,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    remarks VARCHAR(500)
);

CREATE INDEX idx_report_logs_type ON report_logs(report_type);
CREATE INDEX idx_report_logs_status ON report_logs(status);
