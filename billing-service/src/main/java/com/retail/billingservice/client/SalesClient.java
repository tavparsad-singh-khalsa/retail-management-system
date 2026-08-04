package com.retail.billingservice.client;

import com.retail.billingservice.dto.sales.SaleDto;

public interface SalesClient {

    SaleDto getFinalizedSale(Long saleId);

}
