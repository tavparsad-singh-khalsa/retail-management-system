package com.retail.purchase_service.exception;

public class SupplierAlreadyExistsException extends RuntimeException {
    public SupplierAlreadyExistsException(String message) {
        super(message);
    }
}
