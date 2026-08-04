package com.retail.billingservice.exception;

public class InvoiceAlreadyPaidException extends RuntimeException {
    public InvoiceAlreadyPaidException(String message) {
        super(message);
    }
}
