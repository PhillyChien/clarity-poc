package com.aifinancial.clarity.poc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aifinancial.clarity.poc.constant.PermissionConstants;
import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
@RequiredArgsConstructor
public class UsersController {

    private final AdminService adminService;

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Returns a list of all users. Requires SUPER_ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
                    @ApiResponse(responseCode = "403", description = "Access denied")
            }
    )
    @PreAuthorize("hasAuthority('" + PermissionConstants.USERS_VIEW + "')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/role")
    @Operation(
            summary = "Update user role",
            description = "Updates a user's role. Requires SUPER_ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated role"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PreAuthorize("hasAuthority('" + PermissionConstants.USERS_MANAGE + "')")
    public ResponseEntity<MessageResponse> updateUserRole(@Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUserRole(request));
    }
} 