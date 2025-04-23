package com.aifinancial.clarity.poc.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aifinancial.clarity.poc.dto.request.LoginRequest;
import com.aifinancial.clarity.poc.dto.request.RegisterRequest;
import com.aifinancial.clarity.poc.dto.response.ErrorResponse;
import com.aifinancial.clarity.poc.dto.response.MeResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.model.Permission;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.security.UserDetailsImpl;
import com.aifinancial.clarity.poc.service.AuthService;
import com.aifinancial.clarity.poc.service.UsersService;
import com.aifinancial.clarity.poc.service.impl.AuthServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {
    private final AuthService authService;
    private final UsersService usersService;

    public AuthController(AuthService authService, UsersService usersService) {
        this.authService = authService;
        this.usersService = usersService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", 
               description = "Authenticates a user with username and password, returns JWT token in a cookie")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<?> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        // 取得帶有 token 的 User 信息
        MeResponse loginResponse = authService.authenticateUser(loginRequest);
        
        // 取得 token 用於 Cookie
        String token = "";
        
        // 如果返回的是 TokenMeResponse，則提取 token
        if (loginResponse instanceof AuthServiceImpl.TokenMeResponse) {
            token = ((AuthServiceImpl.TokenMeResponse) loginResponse).getToken();
        }
        
        // 創建 HTTP-only Cookie，使用獲取的 token
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)   // Use HTTPS in production
                .path("/")
                .maxAge(24 * 60 * 60) // 24 hours
                .sameSite("None")
                .build();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new MessageResponse("Authentication successful"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", 
               description = "Registers a new user with NORMAL role")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registration successful", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or username/email already taken", 
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> register(
            @Parameter(description = "Registration details", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        MessageResponse messageResponse = authService.registerUser(registerRequest);
        return ResponseEntity.ok(messageResponse);
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", 
               description = "Returns details about the currently authenticated user including role and permissions")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User details retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = MeResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "User not found in database")
    })
    public ResponseEntity<MeResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            // Fetch the full User entity using UsersService
            Optional<User> userOpt = usersService.findUserById(userId);

            for (Permission p : userOpt.get().getRole().getPermissions()) {
                System.out.println("Permission: " + p.getName());
            }
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String roleName = user.getRole() != null ? user.getRole().getName() : "";
                
                // Extract permission names from the Role entity
                Set<String> permissions = user.getRole() != null && user.getRole().getPermissions() != null
                    ? user.getRole().getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet())
                    : Collections.emptySet();
                
                MeResponse response = new MeResponse(
                        "Bearer",
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        roleName,
                        permissions
                );
                return ResponseEntity.ok(response);
            } else {
                // User found in security context but not in DB (should not happen ideally)
                return ResponseEntity.status(404).build(); 
            }
        }
        
        // Not authenticated
        return ResponseEntity.status(401).build();
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user", 
               description = "Logs out the current user by invalidating the authentication cookie")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logout successful", 
                     content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<MessageResponse> logout() {
        // Clear the JWT cookie
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Expired immediately
                .sameSite("None")
                .build();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new MessageResponse("Logout successful"));
    }
} 