import { apiClient } from "./apiClient";
import type { Folder, MessageResponse, Todo, UserResponse } from "./types";

/**
 * 版主服务 - 负责与后端API交互进行版主特定的操作
 * 这些API端点只能由具有Moderator或Super Admin角色的用户访问
 */
export const moderatorService = {
	/**
	 * 获取所有用户列表
	 * @returns 用户数组
	 */
	getAllUsers: (): Promise<UserResponse[]> => {
		return apiClient.get<UserResponse[]>("/moderator/users");
	},

	/**
	 * 获取指定用户的文件夹列表
	 * @param userId 用户ID
	 * @returns 文件夹数组
	 */
	getUserFolders: (userId: number): Promise<Folder[]> => {
		return apiClient.get<Folder[]>(`/moderator/users/${userId}/folders`);
	},

	/**
	 * 获取指定用户的待办事项列表
	 * @param userId 用户ID
	 * @returns 待办事项数组
	 */
	getUserTodos: (userId: number): Promise<Todo[]> => {
		return apiClient.get<Todo[]>(`/moderator/users/${userId}/todos`);
	},

	/**
	 * 切换待办事项的禁用状态
	 * @param todoId 待办事项ID
	 * @returns 包含操作结果消息的响应
	 */
	toggleTodoStatus: (todoId: number): Promise<MessageResponse> => {
		return apiClient.put<MessageResponse>(
			`/moderator/todos/${todoId}/toggle-status`,
			{},
		);
	},

	/**
	 * 禁用待办事项
	 * @param todoId 待办事项ID
	 * @returns 包含操作结果消息的响应
	 */
	disableTodo: (todoId: number): Promise<MessageResponse> => {
		return apiClient.put<MessageResponse>(
			`/moderator/todos/${todoId}/toggle-status`,
			{},
		);
	},

	/**
	 * 启用待办事项
	 * @param todoId 待办事项ID
	 * @returns 包含操作结果消息的响应
	 */
	enableTodo: (todoId: number): Promise<MessageResponse> => {
		return apiClient.put<MessageResponse>(
			`/moderator/todos/${todoId}/toggle-status`,
			{},
		);
	},
};
