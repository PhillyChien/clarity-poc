package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.service.ModeratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moderator")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Moderator", description = "Moderator operations for managing all todos and folders")
@PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
public class ModeratorController {

    private final ModeratorService moderatorService;

    public ModeratorController(ModeratorService moderatorService) {
        this.moderatorService = moderatorService;
    }

    @GetMapping("/folders")
    @Operation(summary = "Get all folders", 
              description = "Returns a list of all folders. Accessible to MODERATOR and SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of folders retrieved successfully", 
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FolderResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have moderator rights")
    })
    public ResponseEntity<List<FolderResponse>> getAllFolders() {
        return ResponseEntity.ok(moderatorService.getAllFolders());
    }

    @GetMapping("/todos")
    @Operation(summary = "Get all todos", 
              description = "Returns a list of all todos. Accessible to MODERATOR and SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of todos retrieved successfully", 
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TodoResponse.class)))),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have moderator rights")
    })
    public ResponseEntity<List<TodoResponse>> getAllTodos() {
        return ResponseEntity.ok(moderatorService.getAllTodos());
    }

    @PutMapping("/todos/{todoId}/toggle-status")
    @Operation(summary = "Toggle todo disabled status", 
              description = "Toggles a todo's disabled status. Accessible to MODERATOR and SUPER_ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todo status toggled successfully", 
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request - Todo not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have moderator rights")
    })
    public ResponseEntity<MessageResponse> toggleTodoStatus(
            @Parameter(description = "ID of the todo to toggle", required = true)
            @PathVariable Long todoId) {
        return ResponseEntity.ok(moderatorService.toggleTodoDisabledStatus(todoId));
    }
} 