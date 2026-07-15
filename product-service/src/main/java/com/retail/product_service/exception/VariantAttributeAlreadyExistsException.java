package com.retail.product_service.exception;

public class VariantAttributeAlreadyExistsException extends RuntimeException {
    public VariantAttributeAlreadyExistsException(String message) {
        super(message);
    }
}
