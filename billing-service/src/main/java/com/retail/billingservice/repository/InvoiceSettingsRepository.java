package com.retail.billingservice.repository;

import com.retail.billingservice.entity.InvoiceSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceSettingsRepository extends JpaRepository<InvoiceSettings, Long> {
}