package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.dto.request.LoginRequest;
import com.aifinancial.clarity.poc.dto.request.RegisterRequest;
import com.aifinancial.clarity.poc.dto.response.JwtResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        MessageResponse messageResponse = authService.registerUser(registerRequest);
        return ResponseEntity.ok(messageResponse);
    }
} 