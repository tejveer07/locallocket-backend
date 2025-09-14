package com.locallocket.backend.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    // Default constructor
    public StockUpdateRequest() {}

    // Constructor
    public StockUpdateRequest(Integer stock) {
        this.stock = stock;
    }

    // Getters and Setters
    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
