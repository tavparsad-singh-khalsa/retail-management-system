package com.retail.purchase_service.exception;

public class InactiveSupplierException extends RuntimeException {
    public InactiveSupplierException(String message) {
        super(message);
    }
}