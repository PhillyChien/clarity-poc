import { apiClient } from "./apiClient";
import type {
	JwtResponse,
	LoginRequest,
	MessageResponse,
	RegisterRequest,
	User,
} from "./types";

/**
 * 认证服务 - 负责与后端API交互进行用户认证相关操作
 */
export const authService = {
	/**
	 * 用户登录
	 * @param credentials 登录凭证
	 * @returns JWT响应
	 */
	login: (credentials: LoginRequest): Promise<JwtResponse> => {
		return apiClient.post<JwtResponse>("/auth/login", credentials, {
			requiresAuth: false,
		});
	},

	/**
	 * 注册新用户
	 * @param registerData 注册数据，包含username、email和password
	 * @returns 消息响应
	 */
	register: (registerData: RegisterRequest): Promise<MessageResponse> => {
		return apiClient.post<MessageResponse>("/auth/register", registerData, {
			requiresAuth: false,
		});
	},

	/**
	 * 从JWT响应构建用户对象
	 * @param jwtResponse 服务器返回的JWT响应
	 * @returns 用户对象
	 */
	buildUserFromJwtResponse: (jwtResponse: JwtResponse): User => {
		return {
			id: jwtResponse.id,
			username: jwtResponse.username,
			email: jwtResponse.email,
			role: jwtResponse.role,
		};
	},
};
