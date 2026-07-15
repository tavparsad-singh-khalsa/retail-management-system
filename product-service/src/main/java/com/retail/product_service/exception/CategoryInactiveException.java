package com.retail.product_service.exception;

public class CategoryInactiveException extends RuntimeException {
    public CategoryInactiveException(String message) {
        super(message);
    }
}
