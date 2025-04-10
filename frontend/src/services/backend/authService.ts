import { apiClient } from "./apiClient";
import type {
	LoginRequest,
	MeResponse,
	MessageResponse,
	RegisterRequest,
} from "./types";

/**
 * 认证服务 - 负责与后端API交互进行用户认证相关操作
 */
export const authService = {
	/**
	 * 用户登录
	 * @param credentials 登录凭证
	 * @returns 登录结果消息
	 */
	login: async (credentials: LoginRequest): Promise<MessageResponse> => {
		// 登录并设置 HTTP-only Cookie，返回登录成功消息
		return await apiClient.post<MessageResponse>("/auth/login", credentials, {
			requiresAuth: false,
			credentials: "include", // 启用 Cookie
		});
	},

	/**
	 * 获取当前用户信息
	 * @returns 用户信息
	 */
	getCurrentUser: async (): Promise<MeResponse> => {
		// 从 Cookie 获取用户信息
		return await apiClient.get<MeResponse>("/auth/me", {
			requiresAuth: false,
			credentials: "include", // 启用 Cookie
		});
	},

	/**
	 * 注册新用户
	 * @param registerData 注册数据，包含username、email和password
	 * @returns 用户信息
	 */
	register: async (registerData: RegisterRequest): Promise<MessageResponse> => {
		// 注册用户
		await apiClient.post<MessageResponse>("/auth/register", registerData, {
			requiresAuth: false,
		});

		// 注册成功后自动登录
		return await authService.login({
			username: registerData.username,
			password: registerData.password,
		});
	},

	/**
	 * 登出用户
	 */
	logout: async (): Promise<void> => {
		try {
			await apiClient.post<MessageResponse>(
				"/auth/logout",
				{},
				{
					credentials: "include",
				},
			);
		} catch (error) {
			console.error("Logout failed", error);
		}
	},
};
