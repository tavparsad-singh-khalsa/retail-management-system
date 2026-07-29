package com.retail.purchase_service.exception;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(String message) {
        super(message);
    }
}
