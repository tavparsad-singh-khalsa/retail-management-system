package com.retail.purchase_service.exception;

public class InvalidPurchaseStateException extends RuntimeException {
    public InvalidPurchaseStateException(String message) {
        super(message);
    }
}