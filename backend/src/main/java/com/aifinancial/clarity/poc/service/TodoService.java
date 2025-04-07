package com.aifinancial.clarity.poc.service;

import com.aifinancial.clarity.poc.dto.request.TodoRequest;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;

import java.util.List;

public interface TodoService {
    
    List<TodoResponse> getAllTodos();
    
    List<TodoResponse> getCurrentUserTodos();
    
    List<TodoResponse> getTodosByFolder(Long folderId);
    
    TodoResponse getTodo(Long id);
    
    TodoResponse createTodo(TodoRequest todoRequest);
    
    TodoResponse updateTodo(Long id, TodoRequest todoRequest);
    
    TodoResponse toggleCompleted(Long id);
    
    void deleteTodo(Long id);
} 