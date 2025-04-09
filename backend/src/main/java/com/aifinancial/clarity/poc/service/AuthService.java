package com.aifinancial.clarity.poc.service;

import com.aifinancial.clarity.poc.dto.request.LoginRequest;
import com.aifinancial.clarity.poc.dto.request.RegisterRequest;
import com.aifinancial.clarity.poc.dto.response.MeResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;

public interface AuthService {
    /**
     * 用戶認證並生成 JWT Token
     */
    MeResponse authenticateUser(LoginRequest loginRequest);
    
    /**
     * 註冊新用戶
     */
    MessageResponse registerUser(RegisterRequest registerRequest);
} 