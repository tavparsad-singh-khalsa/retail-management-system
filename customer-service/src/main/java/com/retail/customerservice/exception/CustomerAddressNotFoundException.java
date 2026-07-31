package com.retail.customerservice.exception;

public class CustomerAddressNotFoundException extends RuntimeException {
    public CustomerAddressNotFoundException(String message) {
        super(message);
    }
}
