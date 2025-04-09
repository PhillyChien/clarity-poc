package com.aifinancial.clarity.poc.service;

import java.util.List;

import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;

/**
 * 用戶管理服務接口，整合了 AdminService 和 ModeratorService 的功能
 */
public interface UsersService {
    /**
     * 獲取所有用戶列表
     * @return 用戶列表
     */
    List<UserResponse> getAllUsers();

    /**
     * 更新用戶角色
     * @param request 角色更新請求
     * @return 操作結果消息
     */
    MessageResponse updateUserRole(RoleUpdateRequest request);
    
    /**
     * 獲取指定用戶的所有文件夾
     * @param userId 用戶ID
     * @return 文件夾列表
     */
    List<FolderResponse> getFoldersByUserId(Long userId);

    /**
     * 獲取指定用戶的所有待辦事項
     * @param userId 用戶ID
     * @return 待辦事項列表
     */
    List<TodoResponse> getTodosByUserId(Long userId);

    /**
     * 切換待辦事項的禁用狀態
     * @param todoId 待辦事項ID
     * @return 操作結果消息
     */
    MessageResponse toggleTodoDisabledStatus(Long todoId);
} 