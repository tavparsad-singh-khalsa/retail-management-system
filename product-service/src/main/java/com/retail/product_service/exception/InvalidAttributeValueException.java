package com.retail.product_service.exception;

public class InvalidAttributeValueException extends RuntimeException {
    public InvalidAttributeValueException(String message) {
        super(message);
    }
}
