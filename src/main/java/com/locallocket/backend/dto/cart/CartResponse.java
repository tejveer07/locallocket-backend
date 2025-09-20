package com.locallocket.backend.dto.cart;

import com.locallocket.backend.entity.Cart;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CartResponse {
    private Long id;
    private Long userId;
    private Long vendorId;
    private String vendorShopName;
    private String vendorAddress;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal platformFee;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private int totalItems;
    private String message; // For vendor switching messages

    // Default constructor
    public CartResponse() {}

    // Constructor from Cart entity
    public CartResponse(Cart cart) {
        this.id = cart.getId();
        this.userId = cart.getUser().getId();
        this.vendorId = cart.getVendor().getId();
        this.vendorShopName = cart.getVendor().getShopName();
        this.vendorAddress = cart.getVendor().getAddress();
        this.items = cart.getItems().stream()
                .map(CartItemResponse::new)
                .collect(Collectors.toList());
        this.subtotal = cart.getSubtotal();
        this.platformFee = cart.getPlatformFee();
        this.deliveryFee = cart.getDeliveryFee();
        this.totalAmount = cart.getTotalAmount();
        this.totalItems = cart.getTotalItems();
    }

    // Constructor with message
    public CartResponse(Cart cart, String message) {
        this(cart);
        this.message = message;
    }

    // Complete Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getVendorShopName() { return vendorShopName; }
    public void setVendorShopName(String vendorShopName) { this.vendorShopName = vendorShopName; }

    public String getVendorAddress() { return vendorAddress; }
    public void setVendorAddress(String vendorAddress) { this.vendorAddress = vendorAddress; }

    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getPlatformFee() { return platformFee; }
    public void setPlatformFee(BigDecimal platformFee) { this.platformFee = platformFee; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
