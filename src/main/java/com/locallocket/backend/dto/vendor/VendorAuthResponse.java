package com.locallocket.backend.dto.vendor;

import com.locallocket.backend.dto.auth.UserDto;

public class VendorAuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private UserDto user;
    private Long vendorId;
    private String message;

    // Default constructor
    public VendorAuthResponse() {}

    // Constructor
    public VendorAuthResponse(String token, UserDto user, Long vendorId, String message) {
        this.token = token;
        this.user = user;
        this.vendorId = vendorId;
        this.message = message;
    }

    // Complete Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
