package com.locallocket.backend.dto.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AddMultipleProductsRequest {
    @NotEmpty(message = "Products list cannot be empty")
    @Valid
    private List<ProductItem> products;

    // Default constructor
    public AddMultipleProductsRequest() {}

    // Constructor
    public AddMultipleProductsRequest(List<ProductItem> products) {
        this.products = products;
    }

    // Getters and Setters
    public List<ProductItem> getProducts() {
        return products;
    }

    public void setProducts(List<ProductItem> products) {
        this.products = products;
    }

    // Inner class for individual product items
    public static class ProductItem {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        // Default constructor
        public ProductItem() {}

        // Constructor
        public ProductItem(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        // Getters and Setters
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
