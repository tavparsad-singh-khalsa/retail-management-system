package com.retail.product_service.exception;

public class StockMovementNotFoundException extends RuntimeException {
    public StockMovementNotFoundException(String message) {
        super(message);
    }
}
