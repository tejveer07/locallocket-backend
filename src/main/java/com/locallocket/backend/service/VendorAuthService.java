// src/main/java/com/locallocket/backend/service/VendorAuthService.java
package com.locallocket.backend.service;

import com.locallocket.backend.dto.auth.LoginRequest;
import com.locallocket.backend.dto.auth.UserDto;
import com.locallocket.backend.dto.vendor.VendorAuthResponse;
import com.locallocket.backend.dto.vendor.VendorSignupRequest;
import com.locallocket.backend.entity.Role;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.entity.Vendor;
import com.locallocket.backend.exception.BadRequestException;
import com.locallocket.backend.exception.UnauthorizedException;
import com.locallocket.backend.repository.UserRepository;
import com.locallocket.backend.repository.VendorRepository;
import com.locallocket.backend.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorAuthService {
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public VendorAuthService(UserRepository ur, VendorRepository vr, PasswordEncoder pe, JwtTokenProvider jtp) {
        this.userRepository = ur; this.vendorRepository = vr; this.passwordEncoder = pe; this.jwtTokenProvider = jtp;
    }

    @Transactional
    public VendorAuthResponse signup(VendorSignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new BadRequestException("Phone already registered");
        }

        // Create user with VENDOR role (NOT CUSTOMER)
        User u = new User();
        u.setEmail(req.getEmail());
        u.setPhoneNumber(req.getPhoneNumber());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setFullName(req.getFullName());

        // CRITICAL: Add VENDOR role, not CUSTOMER
        u.getRoles().clear(); // Clear any default roles
        u.getRoles().add(Role.VENDOR); // Add VENDOR role

        u.setIsActive(true);
        userRepository.save(u);

        // Create vendor profile
        Vendor v = new Vendor();
        v.setUser(u);
        v.setShopName(req.getShopName());
        v.setDescription(req.getDescription());
        v.setAddress(req.getAddress());
        v.setLatitude(req.getLatitude());
        v.setLongitude(req.getLongitude());
        v.setIsActive(true);
        vendorRepository.save(v);

        String token = jwtTokenProvider.generateToken(u.getEmail(), u.getId());
        return new VendorAuthResponse(token, new UserDto(u), v.getId(), "Vendor registration successful");
    }


    @Transactional(readOnly=true)
    public Vendor currentVendor(User user) {
        return vendorRepository.findByUser(user).orElseThrow(() -> new BadRequestException("Vendor profile not found"));
    }

    public VendorAuthResponse login(LoginRequest request) {
        // Find user by email or phone
        User user = userRepository.findByEmailOrPhoneNumber(request.getEmailOrPhone())
                .orElseThrow(() -> new UnauthorizedException("Invalid email/phone or password"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        // Check if user has VENDOR role
        if (!user.getRoles().contains(Role.VENDOR)) {
            throw new UnauthorizedException("Access denied. Vendor account required.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email/phone or password");
        }

        // Get vendor profile
        Vendor vendor = currentVendor(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());

        // Create response
        UserDto userDto = new UserDto(user);
        return new VendorAuthResponse(token, userDto, vendor.getId(), "Login successful");
    }

}
