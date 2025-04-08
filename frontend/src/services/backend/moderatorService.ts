import { apiClient } from "./apiClient";
import type { Folder, MessageResponse, Todo } from "./types";

/**
 * 版主服务 - 负责与后端API交互进行版主特定的操作
 * 这些API端点只能由具有Moderator或Super Admin角色的用户访问
 */
export const moderatorService = {
	/**
	 * 获取所有用户的文件夹列表
	 * @returns 文件夹数组
	 */
	getAllFolders: (): Promise<Folder[]> => {
		return apiClient.get<Folder[]>("/moderator/folders");
	},

	/**
	 * 获取所有用户的Todo列表
	 * @returns Todo数组
	 */
	getAllTodos: (): Promise<Todo[]> => {
		return apiClient.get<Todo[]>("/moderator/todos");
	},

	/**
	 * 切换Todo的禁用状态
	 * @param todoId Todo ID
	 * @returns 包含操作结果消息的响应
	 */
	toggleTodoStatus: (todoId: number): Promise<MessageResponse> => {
		return apiClient.put<MessageResponse>(
			`/moderator/todos/${todoId}/toggle-status`,
			{},
		);
	},
};
