package com.retail.sales_service.service;

import com.retail.sales_service.dto.request.CreateSaleRequest;
import com.retail.sales_service.dto.response.SaleResponse;

import java.util.List;

public interface SaleService {

    SaleResponse createSale(CreateSaleRequest request);

    SaleResponse getSaleById(Long saleId);

    List<SaleResponse> getAllSales();

    SaleResponse cancelSale(Long saleId);
}

