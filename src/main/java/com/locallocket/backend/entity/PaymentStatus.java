package com.locallocket.backend.entity;

public enum PaymentStatus {
    CREATED,    // Payment order created in Razorpay
    PENDING,    // Payment initiated by customer
    SUCCESS,    // Payment successful
    FAILED,     // Payment failed
    REFUNDED    // Payment refunded (full or partial)
}
