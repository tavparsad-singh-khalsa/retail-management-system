package com.retail.product_service.exception;

public class CategoryHasActiveChildrenException extends RuntimeException {
    public CategoryHasActiveChildrenException(String message) {
        super(message);
    }
}
