import { apiClient } from "./apiClient";
import type { MessageResponse, RoleUpdateRequest, UserResponse } from "./types";

/**
 * 管理员服务 - 负责与后端API交互进行管理员特定的操作
 * 这些API端点只能由具有Super Admin角色的用户访问
 */
export const adminService = {
	/**
	 * 获取所有用户列表
	 * @returns 用户数组
	 */
	getAllUsers: (): Promise<UserResponse[]> => {
		return apiClient.get<UserResponse[]>("/admin/users");
	},

	/**
	 * 更新用户角色
	 * @param userId 用户ID
	 * @param role 要设置的角色名称
	 * @returns 操作响应消息
	 */
	updateUserRole: (userId: number, role: string): Promise<MessageResponse> => {
		const request: RoleUpdateRequest = {
			userId,
			role,
		};
		return apiClient.post<MessageResponse>("/admin/users/role", request);
	},
};
