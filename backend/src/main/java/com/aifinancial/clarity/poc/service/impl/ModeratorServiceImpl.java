package com.aifinancial.clarity.poc.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aifinancial.clarity.poc.converter.UserConverter;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Todo;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.FolderRepository;
import com.aifinancial.clarity.poc.repository.TodoRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.service.ModeratorService;

@Service
public class ModeratorServiceImpl implements ModeratorService {

    private final FolderRepository folderRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final UserConverter userConverter;

    public ModeratorServiceImpl(FolderRepository folderRepository, TodoRepository todoRepository, 
                                UserRepository userRepository, UserConverter userConverter) {
        this.folderRepository = folderRepository;
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userConverter.toDtoList(users);
    }

    @Override
    public List<FolderResponse> getFoldersByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Folder> folders = folderRepository.findByOwnerOrderByCreatedAtDesc(user);
        return folders.stream()
                .map(folder -> FolderResponse.builder()
                        .id(folder.getId())
                        .name(folder.getName())
                        .ownerId(folder.getOwner().getId())
                        .createdAt(folder.getCreatedAt())
                        .updatedAt(folder.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TodoResponse> getTodosByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Todo> todos = todoRepository.findByOwnerOrderByCreatedAtDesc(user);
        return todos.stream()
                .map(todo -> TodoResponse.builder()
                        .id(todo.getId())
                        .title(todo.getTitle())
                        .description(todo.getDescription())
                        .completed(todo.isCompleted())
                        .disabled(todo.isDisabled())
                        .ownerId(todo.getOwner().getId())
                        .folderId(todo.getFolder() != null ? todo.getFolder().getId() : null)
                        .createdAt(todo.getCreatedAt())
                        .updatedAt(todo.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse toggleTodoDisabledStatus(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found with id: " + todoId));

        // 切换禁用状态
        todo.setDisabled(!todo.isDisabled());
        todoRepository.save(todo);

        String status = todo.isDisabled() ? "disabled" : "enabled";
        return new MessageResponse("Todo successfully " + status);
    }
} 