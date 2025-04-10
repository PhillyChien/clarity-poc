import { apiClient } from "./apiClient";
import type {
	Folder,
	MessageResponse,
	RoleUpdateRequest,
	Todo,
	UserResponse,
} from "./types";

/**
 * 用戶管理服務 - 負責與後端 API 交互進行用戶管理操作
 * 這些 API 端點根據不同操作需要不同的權限級別
 */
export const usersService = {
	/**
	 * 獲取所有用戶列表
	 * 需要 MODERATOR 或 SUPER_ADMIN 權限
	 * @returns 用戶數組
	 */
	getAllUsers: (): Promise<UserResponse[]> => {
		return apiClient.get<UserResponse[]>("/users");
	},

	/**
	 * 更新用戶角色
	 * 需要 SUPER_ADMIN 權限
	 * @param userId 用戶ID
	 * @param role 要設置的角色名稱
	 * @returns 操作響應消息
	 */
	updateUserRole: (userId: number, role: string): Promise<MessageResponse> => {
		const request: RoleUpdateRequest = {
			userId,
			role,
		};
		return apiClient.post<MessageResponse>("/users/role", request);
	},

	/**
	 * 獲取指定用戶的文件夾列表
	 * 需要 MODERATOR 或 SUPER_ADMIN 權限
	 * @param userId 用戶ID
	 * @returns 文件夾數組
	 */
	getUserFolders: (userId: number): Promise<Folder[]> => {
		return apiClient.get<Folder[]>(`/folders?userId=${userId}`);
	},

	/**
	 * 獲取指定用戶的待辦事項列表
	 * 需要 MODERATOR 或 SUPER_ADMIN 權限
	 * @param userId 用戶ID
	 * @returns 待辦事項數組
	 */
	getUserTodos: (userId: number): Promise<Todo[]> => {
		return apiClient.get<Todo[]>(`/todos?userId=${userId}`);
	},

	/**
	 * 切換待辦事項的禁用狀態
	 * 需要 MODERATOR 或 SUPER_ADMIN 權限
	 * @param todoId 待辦事項ID
	 * @returns 包含操作結果消息的響應
	 */
	toggleTodoStatus: (todoId: number): Promise<MessageResponse> => {
		return apiClient.put<MessageResponse>(
			`/todos/${todoId}/toggle-disabled`,
			{},
		);
	},

	/**
	 * 禁用待辦事項
	 * 需要 MODERATOR 或 SUPER_ADMIN 權限
	 * @param todoId 待辦事項ID
	 * @returns 包含操作結果消息的響應
	 */
	disableTodo: (todoId: number): Promise<MessageResponse> => {
		return apiClient.put<MessageResponse>(
			`/todos/${todoId}/toggle-disabled`,
			{},
		);
	},

	/**
	 * 啟用待辦事項
	 * 需要 MODERATOR 或 SUPER_ADMIN 權限
	 * @param todoId 待辦事項ID
	 * @returns 包含操作結果消息的響應
	 */
	enableTodo: (todoId: number): Promise<MessageResponse> => {
		return apiClient.put<MessageResponse>(
			`/todos/${todoId}/toggle-disabled`,
			{},
		);
	},
};
