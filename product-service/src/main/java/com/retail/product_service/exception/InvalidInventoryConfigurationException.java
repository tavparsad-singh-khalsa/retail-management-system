package com.retail.product_service.exception;

public class InvalidInventoryConfigurationException extends RuntimeException {
    public InvalidInventoryConfigurationException(String message) {
        super(message);
    }
}
