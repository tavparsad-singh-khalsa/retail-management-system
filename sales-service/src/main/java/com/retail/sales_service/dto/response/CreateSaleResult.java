package com.retail.sales_service.dto.response;

/**
 * Result of a create-sale attempt that carries the resolved SaleResponse
 * plus whether the call was an idempotency replay of an existing sale
 * (200) instead of a new creation (201).
 */
public record CreateSaleResult(SaleResponse response, boolean replayed) {
}