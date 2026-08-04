package com.retail.reportservice.client;

import com.retail.reportservice.dto.external.ExternalSaleDto;

import java.time.LocalDate;
import java.util.List;

public interface SalesClient {

    List<ExternalSaleDto> getAllSales();

    List<ExternalSaleDto> getSalesByDateRange(LocalDate startDate, LocalDate endDate);
}
