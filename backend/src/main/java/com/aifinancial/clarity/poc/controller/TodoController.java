package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.dto.request.TodoRequest;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/todos")
@Tag(name = "Todo", description = "Todo management APIs")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get current user's todos", 
               description = "Retrieves all todos owned by the currently authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todos retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<List<TodoResponse>> getCurrentUserTodos() {
        return ResponseEntity.ok(todoService.getCurrentUserTodos());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all todos", 
               description = "Retrieves all todos in the system. Only accessible by MODERATOR and SUPER_ADMIN roles")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todos retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<List<TodoResponse>> getAllTodos() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }

    @GetMapping("/folder/{folderId}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get todos by folder", 
               description = "Retrieves all todos within a specific folder")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todos retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Folder not found")
    })
    public ResponseEntity<List<TodoResponse>> getTodosByFolder(
            @Parameter(description = "ID of the folder to get todos from", required = true)
            @PathVariable Long folderId) {
        return ResponseEntity.ok(todoService.getTodosByFolder(folderId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get a todo by ID", 
               description = "Retrieves a specific todo by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todo retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    public ResponseEntity<TodoResponse> getTodo(
            @Parameter(description = "ID of the todo to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(todoService.getTodo(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new todo", 
               description = "Creates a new todo for the current user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todo created successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<TodoResponse> createTodo(
            @Parameter(description = "Todo details", required = true)
            @Valid @RequestBody TodoRequest todoRequest) {
        return ResponseEntity.ok(todoService.createTodo(todoRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update a todo", 
               description = "Updates an existing todo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todo updated successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    public ResponseEntity<TodoResponse> updateTodo(
            @Parameter(description = "ID of the todo to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated todo details", required = true)
            @Valid @RequestBody TodoRequest todoRequest) {
        return ResponseEntity.ok(todoService.updateTodo(id, todoRequest));
    }

    @PatchMapping("/{id}/toggle-completed")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Toggle todo completion status", 
               description = "Toggles the completed status of a todo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todo status toggled successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    public ResponseEntity<TodoResponse> toggleCompleted(
            @Parameter(description = "ID of the todo to toggle status", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(todoService.toggleCompleted(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a todo", 
               description = "Deletes a todo by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Todo deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Todo not found")
    })
    public ResponseEntity<Void> deleteTodo(
            @Parameter(description = "ID of the todo to delete", required = true)
            @PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }
} 