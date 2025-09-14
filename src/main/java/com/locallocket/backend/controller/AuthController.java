package com.locallocket.backend.controller;

import com.locallocket.backend.dto.auth.*;
import com.locallocket.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("Authorization") String token) {
        // This endpoint will be implemented with JWT filter later
        // For now, it's a placeholder for getting current user details
        return ResponseEntity.ok().build();
    }

    @PutMapping("/location")
    public ResponseEntity<UserDto> updateLocation(
            @RequestHeader("Authorization") String token,
            @RequestBody LocationUpdateRequest request) {
        // This endpoint will be implemented with JWT filter later
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hello")
    public String dummy(){
        return "Hello, World!";
    }
}
