package com.retail.sales_service.exception;

public class InvalidSaleException extends RuntimeException {
    public InvalidSaleException(String message) {
        super(message);
    }
}
