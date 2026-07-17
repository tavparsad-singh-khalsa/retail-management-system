package com.retail.product_service.enums;

public enum MovementType {
    PURCHASE,    // Increases currentStock
    SALE,        // Decreases currentStock
    RETURN,      // Increases currentStock
    ADJUSTMENT,  // Can increase or decrease depending on logic
    RESERVE,     // Increases reservedStock
    UNRESERVE,   // Decreases reservedStock
    DAMAGE       // Decreases currentStock
}