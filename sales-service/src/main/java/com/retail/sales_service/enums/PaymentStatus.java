package com.retail.sales_service.enums;

/**
 * Payment lifecycle status for an order.
 */
public enum PaymentStatus {
    PENDING,
    PARTIALLY_PAID,
    PAID,
    FAILED,
    REFUNDED
}
