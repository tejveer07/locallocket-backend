package com.locallocket.backend.dto.vendor;

import com.locallocket.backend.entity.Vendor;
import java.time.LocalDateTime;

public class VendorResponse {
    private Long id;
    private String shopName;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean isActive;
    private Double distance; // Distance from customer location in meters
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long totalProducts; // Optional: total product count

    // Default constructor
    public VendorResponse() {}

    // Constructor from Vendor entity
    public VendorResponse(Vendor vendor) {
        this.id = vendor.getId();
        this.shopName = vendor.getShopName();
        this.description = vendor.getDescription();
        this.address = vendor.getAddress();
        this.latitude = vendor.getLatitude();
        this.longitude = vendor.getLongitude();
        this.isActive = vendor.getIsActive();
        this.createdAt = vendor.getCreatedAt();
        this.updatedAt = vendor.getUpdatedAt();
    }

    // Constructor with distance
    public VendorResponse(Vendor vendor, Double distance) {
        this(vendor);
        this.distance = distance;
    }

    // Complete Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }
}
