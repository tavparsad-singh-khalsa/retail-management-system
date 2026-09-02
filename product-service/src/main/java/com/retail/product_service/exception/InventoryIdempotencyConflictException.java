package com.retail.product_service.exception;

/**
 * Thrown when an inventory deduction carries an operation reference that was
 * already used for a materially different operation. The conflicting request
 * must not mutate stock (HTTP 409 Conflict).
 */
public class InventoryIdempotencyConflictException extends RuntimeException {
    public InventoryIdempotencyConflictException(String message) {
        super(message);
    }
}