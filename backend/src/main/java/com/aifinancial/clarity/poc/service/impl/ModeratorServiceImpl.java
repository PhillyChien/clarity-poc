package com.aifinancial.clarity.poc.service.impl;

import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Todo;
import com.aifinancial.clarity.poc.repository.FolderRepository;
import com.aifinancial.clarity.poc.repository.TodoRepository;
import com.aifinancial.clarity.poc.service.ModeratorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModeratorServiceImpl implements ModeratorService {

    private final FolderRepository folderRepository;
    private final TodoRepository todoRepository;

    public ModeratorServiceImpl(FolderRepository folderRepository, TodoRepository todoRepository) {
        this.folderRepository = folderRepository;
        this.todoRepository = todoRepository;
    }

    @Override
    public List<FolderResponse> getAllFolders() {
        List<Folder> folders = folderRepository.findAll();
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
    public List<TodoResponse> getAllTodos() {
        List<Todo> todos = todoRepository.findAll();
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
        try {
            Todo todo = todoRepository.findById(todoId)
                    .orElseThrow(() -> new IllegalArgumentException("Todo not found with ID: " + todoId));

            // 切換禁用狀態
            todo.setDisabled(!todo.isDisabled());
            todoRepository.save(todo);

            String status = todo.isDisabled() ? "disabled" : "enabled";
            return new MessageResponse("Todo successfully " + status);
        } catch (Exception e) {
            return new MessageResponse("Failed to toggle todo status: " + e.getMessage());
        }
    }
} 