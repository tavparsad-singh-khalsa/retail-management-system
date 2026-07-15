package com.retail.product_service.exception;

public class BrandInactiveException extends RuntimeException {
    public BrandInactiveException(String message) {
        super(message);
    }
}
