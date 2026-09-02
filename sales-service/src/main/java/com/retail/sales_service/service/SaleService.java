package com.retail.sales_service.service;

import com.retail.sales_service.dto.request.CreateSaleRequest;
import com.retail.sales_service.dto.request.PaymentRequest;
import com.retail.sales_service.dto.response.CreateSaleResult;
import com.retail.sales_service.dto.response.SaleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaleService {

    SaleResponse createSale(CreateSaleRequest request);

    CreateSaleResult createSale(CreateSaleRequest request, String idempotencyKey);

    SaleResponse getSaleById(Long saleId);

    Page<SaleResponse> getAllSales(Pageable pageable);

    SaleResponse cancelSale(Long saleId);

    SaleResponse settlePayment(Long saleId, PaymentRequest request);
}

