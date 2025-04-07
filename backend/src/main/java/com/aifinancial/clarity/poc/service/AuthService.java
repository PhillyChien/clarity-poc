package com.aifinancial.clarity.poc.service;

import com.aifinancial.clarity.poc.dto.request.LoginRequest;
import com.aifinancial.clarity.poc.dto.request.RegisterRequest;
import com.aifinancial.clarity.poc.dto.response.JwtResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
    
    MessageResponse registerUser(RegisterRequest registerRequest);
} 