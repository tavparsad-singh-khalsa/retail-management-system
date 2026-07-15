package com.retail.product_service.exception;

public class InventoryInactiveException extends RuntimeException {
    public InventoryInactiveException(String message) {
        super(message);
    }
}
