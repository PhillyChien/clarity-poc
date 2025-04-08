package com.aifinancial.clarity.poc.service;

import java.util.List;

import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;

public interface ModeratorService {
    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    List<UserResponse> getAllUsers();
    
    /**
     * 获取指定用户的所有文件夹
     * @param userId 用户ID
     * @return 文件夹列表
     */
    List<FolderResponse> getFoldersByUserId(Long userId);

    /**
     * 获取指定用户的所有待办事项
     * @param userId 用户ID
     * @return 待办事项列表
     */
    List<TodoResponse> getTodosByUserId(Long userId);

    /**
     * 切换待办事项的禁用状态
     * @param todoId 待办事项ID
     * @return 操作结果消息
     */
    MessageResponse toggleTodoDisabledStatus(Long todoId);
} 