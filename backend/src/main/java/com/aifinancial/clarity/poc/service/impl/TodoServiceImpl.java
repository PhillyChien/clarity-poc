package com.aifinancial.clarity.poc.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aifinancial.clarity.poc.constant.RoleConstants;
import com.aifinancial.clarity.poc.dto.request.TodoRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.exception.UnauthorizedException;
import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.Todo;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.FolderRepository;
import com.aifinancial.clarity.poc.repository.TodoRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.security.UserDetailsImpl;
import com.aifinancial.clarity.poc.service.TodoService;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public TodoServiceImpl(TodoRepository todoRepository, FolderRepository folderRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isCurrentUserModeratorOrAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR") || 
                        a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getCurrentUserTodos() {
        User currentUser = getCurrentUser();
        return todoRepository.findByOwnerOrderByCreatedAtDesc(currentUser).stream()
                .map(this::mapToTodoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getTodosByFolder(Long folderId) {
        User currentUser = getCurrentUser();
        
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + folderId));
        
        // If the user is the owner or moderator/admin, they can view the todos in this folder
        if (folder.getOwner().getId().equals(currentUser.getId()) || isCurrentUserModeratorOrAdmin()) {
            return todoRepository.findByFolderOrderByCreatedAtDesc(folder).stream()
                    .map(this::mapToTodoResponse)
                    .collect(Collectors.toList());
        }
        
        throw new UnauthorizedException("Not authorized to view todos in this folder");
    }

    @Override
    @Transactional(readOnly = true)
    public TodoResponse getTodo(Long id) {
        User currentUser = getCurrentUser();
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));

        // Check if the user is owner or moderator/admin
        if (!todo.getOwner().getId().equals(currentUser.getId()) && !isCurrentUserModeratorOrAdmin()) {
            throw new UnauthorizedException("Not authorized to view this todo");
        }

        return mapToTodoResponse(todo);
    }

    @Override
    @Transactional
    public TodoResponse createTodo(TodoRequest todoRequest) {
        User currentUser = getCurrentUser();
        
        Todo todo = new Todo();
        todo.setTitle(todoRequest.getTitle());
        todo.setDescription(todoRequest.getDescription());
        todo.setCompleted(todoRequest.isCompleted());
        todo.setDisabled(false); // Default to not disabled
        todo.setOwner(currentUser);
        
        // Set folder if provided
        if (todoRequest.getFolderId() != null) {
            Folder folder = folderRepository.findById(todoRequest.getFolderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + todoRequest.getFolderId()));
            
            // Ensure the folder belongs to the current user
            if (!folder.getOwner().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("Not authorized to add todo to this folder");
            }
            
            todo.setFolder(folder);
        }
        
        todo = todoRepository.save(todo);
        return mapToTodoResponse(todo);
    }

    @Override
    @Transactional
    public TodoResponse updateTodo(Long id, TodoRequest todoRequest) {
        User currentUser = getCurrentUser();
        
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
        
        // Check if the current user is the owner
        if (!todo.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not authorized to update this todo");
        }
        
        todo.setTitle(todoRequest.getTitle());
        todo.setDescription(todoRequest.getDescription());
        todo.setCompleted(todoRequest.isCompleted());
        
        // Update folder if provided
        if (todoRequest.getFolderId() != null) {
            Folder folder = folderRepository.findById(todoRequest.getFolderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + todoRequest.getFolderId()));
            
            // Ensure the folder belongs to the current user
            if (!folder.getOwner().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("Not authorized to move todo to this folder");
            }
            
            todo.setFolder(folder);
        } else {
            todo.setFolder(null);
        }
        
        todo = todoRepository.save(todo);
        return mapToTodoResponse(todo);
    }

    @Override
    @Transactional
    public TodoResponse toggleCompleted(Long id) {
        User currentUser = getCurrentUser();
        
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
        
        // Check if the current user is the owner
        if (!todo.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not authorized to update this todo");
        }
        
        todo.setCompleted(!todo.isCompleted());
        todo = todoRepository.save(todo);
        return mapToTodoResponse(todo);
    }

    @Override
    @Transactional
    public void deleteTodo(Long id) {
        User currentUser = getCurrentUser();
        
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));
        
        // Check if the current user is the owner or admin
        if (!todo.getOwner().getId().equals(currentUser.getId()) && 
                ! (currentUser.getRole() != null && RoleConstants.ROLE_SUPER_ADMIN.equalsIgnoreCase(currentUser.getRole().getName())) ) {
            throw new UnauthorizedException("Not authorized to delete this todo");
        }
        
        todoRepository.delete(todo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getTodosByUserId(Long userId) {
        // Check if current user has moderator or admin privileges
        if (!isCurrentUserModeratorOrAdmin()) {
            throw new UnauthorizedException("Not authorized to view todos for this user");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Todo> todos = todoRepository.findByOwnerOrderByCreatedAtDesc(user);
        return todos.stream()
                .map(this::mapToTodoResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public MessageResponse toggleTodoDisabledStatus(Long id) {
        // Check if current user has moderator or admin privileges
        if (!isCurrentUserModeratorOrAdmin()) {
            throw new UnauthorizedException("Not authorized to toggle todo disabled status");
        }
        
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + id));

        // Toggle disabled status
        todo.setDisabled(!todo.isDisabled());
        todoRepository.save(todo);

        String status = todo.isDisabled() ? "disabled" : "enabled";
        return new MessageResponse("Todo successfully " + status);
    }
    
    private TodoResponse mapToTodoResponse(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .completed(todo.isCompleted())
                .disabled(todo.isDisabled())
                .ownerId(todo.getOwner().getId())
                .ownerUsername(todo.getOwner().getUsername())
                .folderId(todo.getFolder() != null ? todo.getFolder().getId() : null)
                .folderName(todo.getFolder() != null ? todo.getFolder().getName() : null)
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
} 