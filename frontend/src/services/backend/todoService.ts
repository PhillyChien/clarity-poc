import { apiClient } from "./apiClient";
import type { CreateTodoRequest, Todo, UpdateTodoRequest } from "./types";

/**
 * Todo服务 - 负责与后端API交互进行Todo相关操作
 */
export const todoService = {
	/**
	 * 获取当前用户的所有Todo
	 * @returns Todo数组
	 */
	getUserTodos: (): Promise<Todo[]> => {
		return apiClient.get<Todo[]>("/todos");
	},

	/**
	 * 获取特定文件夹内的Todo列表
	 * @param folderId 文件夹ID
	 * @returns Todo数组
	 */
	getTodosByFolder: (folderId: number): Promise<Todo[]> => {
		return apiClient.get<Todo[]>(`/todos/folder/${folderId}`);
	},

	/**
	 * 获取单个Todo详情
	 * @param todoId Todo ID
	 * @returns Todo对象
	 */
	getTodo: (todoId: number): Promise<Todo> => {
		return apiClient.get<Todo>(`/todos/${todoId}`);
	},

	/**
	 * 创建新Todo
	 * @param todoData Todo数据
	 * @returns 创建的Todo对象
	 */
	createTodo: (todoData: CreateTodoRequest): Promise<Todo> => {
		return apiClient.post<Todo>("/todos", todoData);
	},

	/**
	 * 更新Todo
	 * @param todoId Todo ID
	 * @param todoData 更新的Todo数据
	 * @returns 更新后的Todo对象
	 */
	updateTodo: (todoId: number, todoData: UpdateTodoRequest): Promise<Todo> => {
		return apiClient.put<Todo>(`/todos/${todoId}`, todoData);
	},

	/**
	 * 切换Todo的完成状态
	 * @param todoId Todo ID
	 * @returns 更新后的Todo对象
	 */
	toggleTodoCompletion: (todoId: number): Promise<Todo> => {
		console.log(`API call: toggle completion for todo ${todoId}`);
		return apiClient
			.patch<Todo>(`/todos/${todoId}/toggle-completed`, {})
			.then((response) => {
				console.log(`API response success: todo ${todoId} completion toggled`);
				return response;
			})
			.catch((error) => {
				console.error(`API error toggling todo ${todoId}:`, error);
				throw error;
			});
	},

	/**
	 * 删除Todo
	 * @param todoId Todo ID
	 * @returns void
	 */
	deleteTodo: (todoId: number): Promise<void> => {
		return apiClient.delete<void>(`/todos/${todoId}`);
	},
};
