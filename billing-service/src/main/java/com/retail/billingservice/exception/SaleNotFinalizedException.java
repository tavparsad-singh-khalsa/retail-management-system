package com.retail.billingservice.exception;

public class SaleNotFinalizedException extends RuntimeException {
    public SaleNotFinalizedException(String message) {
        super(message);
    }
}
