package com.retail.purchase_service.exception;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(String message) {
        super(message);
    }
}