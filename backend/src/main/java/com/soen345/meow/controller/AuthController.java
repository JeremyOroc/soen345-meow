package com.soen345.meow.controller;

import com.soen345.meow.dto.AuthResponse;
import com.soen345.meow.dto.LoginRequest;
import com.soen345.meow.dto.SignupRequest;
import com.soen345.meow.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            AuthResponse response = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            if (e.getMessage().contains("At least one of email or phone")) {
                error.put("error", "At least one of email or phone must be provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (e.getMessage().contains("Password is required")) {
                error.put("error", "Password is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (e.getMessage().contains("Email already registered")) {
                error.put("error", "Email already registered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            } else if (e.getMessage().contains("Phone number already registered")) {
                error.put("error", "Phone number already registered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            } else {
                error.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            if (e.getMessage().contains("Email is required")) {
                error.put("error", "Email is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (e.getMessage().contains("Password is required")) {
                error.put("error", "Password is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (e.getMessage().contains("Invalid email or password")) {
                error.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            } else {
                error.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        }
    }
}
