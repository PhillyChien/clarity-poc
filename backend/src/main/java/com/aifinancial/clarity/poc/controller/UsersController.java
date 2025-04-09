package com.aifinancial.clarity.poc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.ErrorResponse;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.service.UsersService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Users", description = "User management operations for administrators and moderators")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
    @Operation(summary = "Get all users", 
              description = "Returns a list of all users. Accessible to MODERATOR and SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of users retrieved successfully", 
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have required rights")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(usersService.getAllUsers());
    }

    @PostMapping("/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update user role", 
              description = "Updates a user's role (e.g., promote to MODERATOR). Only accessible to SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User role updated successfully", 
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request - Invalid role", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have admin rights or attempted to promote to SUPER_ADMIN", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> updateUserRole(
            @Parameter(description = "Role update details", required = true)
            @Valid @RequestBody RoleUpdateRequest roleUpdateRequest) {
        return ResponseEntity.ok(usersService.updateUserRole(roleUpdateRequest));
    }

    @GetMapping("/{userId}/folders")
    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
    @Operation(summary = "Get folders by user ID", 
              description = "Returns a list of folders belonging to the specified user. Accessible to MODERATOR and SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of folders retrieved successfully", 
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FolderResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have moderator rights"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<FolderResponse>> getFoldersByUserId(
            @Parameter(description = "ID of the user to get folders for", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(usersService.getFoldersByUserId(userId));
    }

    @GetMapping("/{userId}/todos")
    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
    @Operation(summary = "Get todos by user ID", 
              description = "Returns a list of todos belonging to the specified user. Accessible to MODERATOR and SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of todos retrieved successfully", 
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TodoResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have moderator rights"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<TodoResponse>> getTodosByUserId(
            @Parameter(description = "ID of the user to get todos for", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(usersService.getTodosByUserId(userId));
    }

    @PutMapping("/todos/{todoId}/toggle-status")
    @PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
    @Operation(summary = "Toggle todo disabled status", 
              description = "Toggles a todo's disabled status. Accessible to MODERATOR and SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todo status toggled successfully", 
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have moderator rights", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Todo not found", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> toggleTodoStatus(
            @Parameter(description = "ID of the todo to toggle", required = true)
            @PathVariable Long todoId) {
        return ResponseEntity.ok(usersService.toggleTodoDisabledStatus(todoId));
    }
} 