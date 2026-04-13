package com.soen345.meow.service;

import com.soen345.meow.dto.AuthResponse;
import com.soen345.meow.dto.LoginRequest;
import com.soen345.meow.dto.SignupRequest;
import com.soen345.meow.entity.User;
import com.soen345.meow.repository.UserRepository;
import com.soen345.meow.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse signup(SignupRequest request) {
        // Validate that at least one of email or phone is provided
        if ((request.getEmail() == null || request.getEmail().isEmpty()) &&
            (request.getPhone() == null || request.getPhone().isEmpty())) {
            throw new IllegalArgumentException("At least one of email or phone must be provided");
        }

        // Validate password is provided
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Check if email already exists
        if (request.getEmail() != null && !request.getEmail().isEmpty() && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check if phone already exists
        if (request.getPhone() != null && !request.getPhone().isEmpty() && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        // Create new user
        String email = request.getEmail() != null && !request.getEmail().isEmpty() ? request.getEmail() : null;
        String phone = request.getPhone() != null && !request.getPhone().isEmpty() ? request.getPhone() : null;
        String passwordHash = passwordEncoder.encode(request.getPassword());

        User user = new User(email, phone, passwordHash, "CUSTOMER");
        userRepository.save(user);

        return new AuthResponse(null, "User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        // Validate email is provided
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Validate password is provided
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Try to find user by email first, then by phone
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> userRepository.findByPhone(request.getEmail()).orElse(null));

        // Check if user exists and password matches
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail() != null ? user.getEmail() : user.getPhone(), user.getRole());

        return new AuthResponse(token, "Login successful");
    }
}
