package com.retail.reportservice.exception;

public class InvalidReportRequestException extends RuntimeException {
    public InvalidReportRequestException(String message) {
        super(message);
    }
}
