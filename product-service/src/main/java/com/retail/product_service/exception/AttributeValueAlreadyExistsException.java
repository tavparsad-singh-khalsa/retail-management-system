package com.retail.product_service.exception;

public class AttributeValueAlreadyExistsException extends RuntimeException {
    public AttributeValueAlreadyExistsException(String message) {
        super(message);
    }
}
