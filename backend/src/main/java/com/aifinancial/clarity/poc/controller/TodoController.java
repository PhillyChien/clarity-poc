package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.dto.request.TodoRequest;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TodoResponse>> getCurrentUserTodos() {
        return ResponseEntity.ok(todoService.getCurrentUserTodos());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TodoResponse>> getAllTodos() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }

    @GetMapping("/folder/{folderId}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TodoResponse>> getTodosByFolder(@PathVariable Long folderId) {
        return ResponseEntity.ok(todoService.getTodosByFolder(folderId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<TodoResponse> getTodo(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.getTodo(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody TodoRequest todoRequest) {
        return ResponseEntity.ok(todoService.createTodo(todoRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest todoRequest) {
        return ResponseEntity.ok(todoService.updateTodo(id, todoRequest));
    }

    @PatchMapping("/{id}/toggle-completed")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<TodoResponse> toggleCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.toggleCompleted(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }
} 