package com.aifinancial.clarity.poc.service;

import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;

import java.util.List;

public interface ModeratorService {
    /**
     * 獲取所有文件夾列表
     * @return 文件夾列表
     */
    List<FolderResponse> getAllFolders();

    /**
     * 獲取所有待辦事項列表
     * @return 待辦事項列表
     */
    List<TodoResponse> getAllTodos();

    /**
     * 切換待辦事項的禁用狀態
     * @param todoId 待辦事項ID
     * @return 操作結果消息
     */
    MessageResponse toggleTodoDisabledStatus(Long todoId);
} 