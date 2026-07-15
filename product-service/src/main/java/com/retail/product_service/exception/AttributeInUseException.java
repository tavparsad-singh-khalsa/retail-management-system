package com.retail.product_service.exception;

public class AttributeInUseException extends RuntimeException {
    public AttributeInUseException(String message) {
        super(message);
    }
}
