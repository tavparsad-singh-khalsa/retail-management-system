package com.retail.billingservice.exception;

public class InvoiceSettingsNotFoundException extends RuntimeException {
    public InvoiceSettingsNotFoundException(String message) {
        super(message);
    }
}