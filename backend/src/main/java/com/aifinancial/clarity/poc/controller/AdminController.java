package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Admin", description = "Admin operations for user management")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", 
              description = "Returns a list of all users. Only accessible to SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of users retrieved successfully", 
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have admin rights")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users/role")
    @Operation(summary = "Update user role", 
              description = "Updates a user's role (e.g., promote to MODERATOR). Only accessible to SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User role updated successfully", 
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request - User not found or invalid role"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have admin rights")
    })
    public ResponseEntity<MessageResponse> updateUserRole(
            @Parameter(description = "Role update details", required = true)
            @Valid @RequestBody RoleUpdateRequest roleUpdateRequest) {
        return ResponseEntity.ok(adminService.updateUserRole(roleUpdateRequest));
    }
} 