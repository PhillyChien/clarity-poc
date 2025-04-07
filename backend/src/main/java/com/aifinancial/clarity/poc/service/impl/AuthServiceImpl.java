package com.aifinancial.clarity.poc.service.impl;

import com.aifinancial.clarity.poc.dto.request.LoginRequest;
import com.aifinancial.clarity.poc.dto.request.RegisterRequest;
import com.aifinancial.clarity.poc.dto.response.JwtResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.security.JwtTokenProvider;
import com.aifinancial.clarity.poc.security.UserDetailsImpl;
import com.aifinancial.clarity.poc.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("");

        return new JwtResponse(
                jwt,
                "Bearer",
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role
        );
    }

    @Override
    public MessageResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }

        // Create new user's account with NORMAL role
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.NORMAL)
                .build();

        userRepository.save(user);

        return new MessageResponse("User registered successfully!");
    }
} 