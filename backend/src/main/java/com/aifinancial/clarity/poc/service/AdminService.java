package com.aifinancial.clarity.poc.service;

import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;

import java.util.List;

public interface AdminService {
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
} 