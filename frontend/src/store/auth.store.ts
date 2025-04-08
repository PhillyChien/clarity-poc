import { create } from "zustand";
import { persist } from "zustand/middleware";
import { authService } from "../services/backend";
import type { User } from "../services/backend/types";

interface AuthState {
	isAuthenticated: boolean;
	token: string | null;
	user: User | null;
	isLoading: boolean;
	error: string | null;

	// 动作
	login: (username: string, password: string) => Promise<void>;
	register: (
		username: string,
		email: string,
		password: string,
	) => Promise<void>;
	logout: () => void;
	setUser: (user: User | null) => void;
	clearError: () => void;
}

// 创建认证存储
export const useAuthStore = create<AuthState>()(
	persist(
		(set) => ({
			isAuthenticated: false,
			token: null,
			user: null,
			isLoading: false,
			error: null,

			// 登录动作
			login: async (username: string, password: string) => {
				try {
					set({ isLoading: true, error: null });
					const response = await authService.login({ username, password });

					// 保存token和用户信息
					const user = authService.buildUserFromJwtResponse(response);
					set({
						token: response.token,
						user: user,
						isAuthenticated: true,
						isLoading: false,
					});
				} catch (error) {
					set({
						isLoading: false,
						error: error instanceof Error ? error.message : "登录失败",
						isAuthenticated: false,
						token: null,
						user: null,
					});
				}
			},

			// 注册动作
			register: async (username: string, email: string, password: string) => {
				try {
					set({ isLoading: true, error: null });
					// 注册用户
					await authService.register({ username, email, password });

					// 注册成功后自动登录
					const loginResponse = await authService.login({ username, password });
					const user = authService.buildUserFromJwtResponse(loginResponse);

					set({
						token: loginResponse.token,
						user: user,
						isAuthenticated: true,
						isLoading: false,
					});
				} catch (error) {
					set({
						isLoading: false,
						error: error instanceof Error ? error.message : "注册失败",
						isAuthenticated: false,
						token: null,
						user: null,
					});
				}
			},

			// 登出动作
			logout: () => {
				set({
					isAuthenticated: false,
					token: null,
					user: null,
					error: null,
				});
			},

			// 设置用户
			setUser: (user: User | null) => {
				set({ user });
			},

			// 清除错误
			clearError: () => {
				set({ error: null });
			},
		}),
		{
			name: "auth-storage", // 本地存储的键名
			partialize: (state) => ({
				token: state.token,
				user: state.user,
			}),
		},
	),
);

// 提供一个获取token的辅助函数供apiClient使用
export const getAuthToken = (): string | null => {
	return useAuthStore.getState().token;
};

// 提供辅助函数检查用户是否具有特定角色
export const hasRole = (role: string): boolean => {
	const user = useAuthStore.getState().user;
	if (!user) return false;

	return user.role === role;
};

// 提供辅助函数检查用户是否是管理员（Super Admin）
export const isAdmin = (): boolean => {
	return hasRole("SUPER_ADMIN");
};

// 提供辅助函数检查用户是否是版主（Moderator）
export const isModerator = (): boolean => {
	return hasRole("MODERATOR") || hasRole("SUPER_ADMIN");
};
