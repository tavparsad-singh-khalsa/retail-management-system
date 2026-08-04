package com.retail.reportservice.client;

import com.retail.reportservice.dto.external.ExternalPurchaseDto;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseClient {

    List<ExternalPurchaseDto> getAllPurchases();

    List<ExternalPurchaseDto> getPurchasesByDateRange(LocalDate startDate, LocalDate endDate);
}
