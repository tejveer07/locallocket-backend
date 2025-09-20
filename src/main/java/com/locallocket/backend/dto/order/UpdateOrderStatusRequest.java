package com.locallocket.backend.dto.order;

import jakarta.validation.constraints.Size;

public class UpdateOrderStatusRequest {
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    private Integer estimatedDeliveryMinutes; // For accepted orders

    // Default constructor
    public UpdateOrderStatusRequest() {}

    // Constructor
    public UpdateOrderStatusRequest(String reason) {
        this.reason = reason;
    }

    // Getters and Setters
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getEstimatedDeliveryMinutes() { return estimatedDeliveryMinutes; }
    public void setEstimatedDeliveryMinutes(Integer estimatedDeliveryMinutes) { this.estimatedDeliveryMinutes = estimatedDeliveryMinutes; }
}
