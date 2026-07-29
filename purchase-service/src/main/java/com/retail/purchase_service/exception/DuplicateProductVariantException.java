package com.retail.purchase_service.exception;

public class DuplicateProductVariantException extends RuntimeException {
    public DuplicateProductVariantException(String message) {
        super(message);
    }
}
