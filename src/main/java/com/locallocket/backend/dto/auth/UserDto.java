package com.locallocket.backend.dto.auth;

import com.locallocket.backend.entity.User;

public class UserDto {
    private Long id;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String address;
    private Double latitude;
    private Double longitude;

    // Constructors
    public UserDto() {}

    public UserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.fullName = user.getFullName();
        this.address = user.getAddress();
        this.latitude = user.getLatitude();
        this.longitude = user.getLongitude();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}

