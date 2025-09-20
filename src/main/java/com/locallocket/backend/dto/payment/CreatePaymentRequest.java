package com.locallocket.backend.dto.payment;

import jakarta.validation.constraints.NotNull;

public class CreatePaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    // Default constructor
    public CreatePaymentRequest() {}

    // Constructor
    public CreatePaymentRequest(Long orderId) {
        this.orderId = orderId;
    }

    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
}