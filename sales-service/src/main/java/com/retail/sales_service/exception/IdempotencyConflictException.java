package com.retail.sales_service.exception;

/**
 * Thrown when an Idempotency-Key is reused with a different request payload
 * than the one that originally created the sale, or when a genuine idempotency
 * race cannot otherwise be resolved. Mapped to HTTP 409 Conflict.
 */
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String message) {
        super(message);
    }
}