package com.retail.product_service.exception;

public class AttributeValueNotFoundException extends RuntimeException {
    public AttributeValueNotFoundException(String message) {
        super(message);
    }
}
