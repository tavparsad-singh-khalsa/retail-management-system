package com.retail.product_service.exception;

public class InvalidStockMovementException extends RuntimeException {
    public InvalidStockMovementException(String message) {
        super(message);
    }
}
