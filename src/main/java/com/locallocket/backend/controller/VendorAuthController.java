package com.locallocket.backend.controller;

import com.locallocket.backend.dto.auth.LoginRequest;
import com.locallocket.backend.dto.auth.UserDto;
import com.locallocket.backend.dto.vendor.VendorAuthResponse;
import com.locallocket.backend.dto.vendor.VendorSignupRequest;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.service.AuthService;
import com.locallocket.backend.service.VendorAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/auth")
@CrossOrigin(origins = "*")
public class VendorAuthController {

    @Autowired
    private VendorAuthService vendorAuthService;

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<VendorAuthResponse> signup(@Valid @RequestBody VendorSignupRequest request) {
        VendorAuthResponse response = vendorAuthService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<VendorAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Use existing login but ensure user has VENDOR role
        VendorAuthResponse response = vendorAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        UserDto userDto = new UserDto(currentUser);
        return ResponseEntity.ok(userDto);
    }
}
