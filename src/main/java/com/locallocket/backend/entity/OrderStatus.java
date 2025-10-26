package com.locallocket.backend.entity;

public enum OrderStatus {
    PENDING,        // Order placed, waiting for vendor acceptance
    ACCEPTED,       // Vendor accepted the order
    REJECTED,       // Vendor rejected the order,    // Order is being prepared
    READY, // Order is out for delivery
    DELIVERED,      // Order successfully delivered
    INPROGRESS, OUTFORDELIVERY, CANCELLED       // Order cancelled by customer or system
}
