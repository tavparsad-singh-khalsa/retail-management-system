package com.retail.billingservice.exception;

public class InvalidInvoiceException extends RuntimeException {

    public InvalidInvoiceException(String message) {
        super(message);
    }
}
