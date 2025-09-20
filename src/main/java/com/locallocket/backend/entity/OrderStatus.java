package com.locallocket.backend.entity;

public enum OrderStatus {
    PENDING,        // Order placed, waiting for vendor acceptance
    ACCEPTED,       // Vendor accepted the order
    REJECTED,       // Vendor rejected the order
    IN_PROGRESS,    // Order is being prepared
    READY,          // Order is ready for delivery/pickup
    OUT_FOR_DELIVERY, // Order is out for delivery
    DELIVERED,      // Order successfully delivered
    CANCELLED       // Order cancelled by customer or system
}
