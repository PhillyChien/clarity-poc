package com.aifinancial.clarity.poc.service.impl;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aifinancial.clarity.poc.constant.RoleConstants;
import com.aifinancial.clarity.poc.dto.request.LoginRequest;
import com.aifinancial.clarity.poc.dto.request.RegisterRequest;
import com.aifinancial.clarity.poc.dto.response.MeResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.exception.BadRequestException;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.RoleRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.security.JwtTokenProvider;
import com.aifinancial.clarity.poc.security.UserDetailsImpl;
import com.aifinancial.clarity.poc.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * Temporary class to return MeResponse with token for login
     */
    public static class TokenMeResponse extends MeResponse {
        private String token;
        
        public TokenMeResponse(String type, Long id, String username, String email, String role, Set<String> permissions, String token) {
            super(type, id, username, email, role, permissions);
            this.token = token;
        }
        
        public String getToken() {
            return token;
        }
    }

    @Override
    public MeResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_")) 
                .findFirst()
                .map(authority -> authority.replace("ROLE_", ""))
                .orElse(""); 
        
        Set<String> permissions = userDetails.getAuthorities().stream()
                                      .map(GrantedAuthority::getAuthority)
                                      .filter(auth -> !auth.startsWith("ROLE_"))
                                      .collect(Collectors.toSet());

        return new TokenMeResponse(
                "Bearer",
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role,
                permissions,
                jwt
        );
    }

    @Override
    @Transactional
    public MessageResponse registerUser(RegisterRequest registerRequest) {
        try {
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                throw new BadRequestException("Username is already taken");
            }

            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }

            Role normalRole = roleRepository.findByName(RoleConstants.ROLE_NORMAL)
                    .orElseThrow(() -> new NoSuchElementException("NORMAL role not found in database. Please initialize roles first."));

            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(normalRole);

            userRepository.save(user);

            return new MessageResponse("User registered successfully!");
        } catch (BadRequestException e) {
            throw e;
        } catch (NoSuchElementException e) {
             throw new RuntimeException("Server configuration error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BadRequestException("Registration failed due to an unexpected error.");
        }
    }
} 