package com.locallocket.backend.service;


import com.locallocket.backend.dto.auth.*;
import com.locallocket.backend.entity.Role;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.exception.BadRequestException;
import com.locallocket.backend.exception.UnauthorizedException;
import com.locallocket.backend.repository.UserRepository;
import com.locallocket.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber())) {
            throw new BadRequestException("User already exists with this email or phone number");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());
        user.setAddress(request.getAddress());

        // ADD CUSTOMER ROLE - This was missing!
        user.getRoles().add(Role.CUSTOMER);

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(savedUser.getEmail(), savedUser.getId());

        // Create response
        UserDto userDto = new UserDto(savedUser);
        return new AuthResponse(token, userDto, "Registration successful");
    }


    public AuthResponse login(LoginRequest request) {
        // Find user by email or phone
        User user = userRepository.findByEmailOrPhoneNumber(request.getEmailOrPhone())
                .orElseThrow(() -> new UnauthorizedException("Invalid email/phone or password"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email/phone or password");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());

        // Create response
        UserDto userDto = new UserDto(user);
        return new AuthResponse(token, userDto, "Login successful");
    }

    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return new UserDto(user);
    }

    public UserDto updateUserLocation(Long userId, Double latitude, Double longitude, String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        user.setLatitude(latitude);
        user.setLongitude(longitude);
        user.setAddress(address);

        User updatedUser = userRepository.save(user);
        return new UserDto(updatedUser);
    }
}

