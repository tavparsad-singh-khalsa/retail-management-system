package com.retail.sales_service.exception;

public class SaleHasActiveInvoiceException extends RuntimeException {

    public SaleHasActiveInvoiceException(Long saleId) {
        super("Cannot cancel sale " + saleId + " because it has an active invoice. Please cancel the invoice first.");
    }
}
