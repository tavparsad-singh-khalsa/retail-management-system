package com.retail.purchase_service.exception;

public class InventoryUpdateException extends RuntimeException {
    public InventoryUpdateException(String message) {
        super(message);
    }
}
