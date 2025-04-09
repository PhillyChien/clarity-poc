package com.aifinancial.clarity.poc.service;

import java.util.List;

import com.aifinancial.clarity.poc.dto.request.TodoRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;

public interface TodoService {
    
    List<TodoResponse> getCurrentUserTodos();
    
    List<TodoResponse> getTodosByFolder(Long folderId);
    
    TodoResponse getTodo(Long id);
    
    TodoResponse createTodo(TodoRequest todoRequest);
    
    TodoResponse updateTodo(Long id, TodoRequest todoRequest);
    
    TodoResponse toggleCompleted(Long id);
    
    void deleteTodo(Long id);
    
    List<TodoResponse> getTodosByUserId(Long userId);
    
    MessageResponse toggleTodoDisabledStatus(Long id);
} 