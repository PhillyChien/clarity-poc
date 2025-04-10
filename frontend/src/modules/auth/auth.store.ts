import { create } from "zustand";
import { persist } from "zustand/middleware";
import { authService } from "../../services/backend";
import type { User } from "../../services/backend/types";
import type { UserRole } from "./types";

// Forward declare role store to avoid circular dependency
let setCurrentRole: ((role: UserRole | null) => void) | null = null;

// Function to be called by role store to set its setCurrentRole function
export const registerRoleStoreSetCurrentRole = (
	fn: (role: UserRole | null) => void,
) => {
	setCurrentRole = fn;
};

interface AuthState {
	isAuthenticated: boolean;
	user: User | null;
	isLoading: boolean;
	error: string | null;

	// Actions
	login: (username: string, password: string) => Promise<void>;
	register: (
		username: string,
		email: string,
		password: string,
	) => Promise<void>;
	logout: () => Promise<void>;
	checkAuth: () => Promise<void>;
	setUser: (user: User | null) => void;
	clearError: () => void;
}

// Create authentication store
export const useAuthStore = create<AuthState>()(
	persist(
		(set) => ({
			isAuthenticated: false,
			user: null,
			isLoading: false,
			error: null,

			// Login action
			login: async (username: string, password: string) => {
				try {
					set({ isLoading: true, error: null });

					// 先調用登錄接口
					await authService.login({ username, password });

					// 再獲取用戶信息
					const userInfo = await authService.getCurrentUser();

					set({
						user: userInfo,
						isAuthenticated: true,
						isLoading: false,
						error: null,
					});

					// Update role in role store
					if (setCurrentRole) {
						setCurrentRole(userInfo.role as UserRole);
					}
				} catch (error) {
					set({
						isLoading: false,
						error: error instanceof Error ? error.message : "Login failed",
						isAuthenticated: false,
						user: null,
					});

					// Clear role in role store
					if (setCurrentRole) {
						setCurrentRole(null);
					}
				}
			},

			// Register action
			register: async (username: string, email: string, password: string) => {
				try {
					set({ isLoading: true, error: null });

					// 先调用注册接口
					await authService.register({ username, email, password });

					// 注册成功后获取用户信息
					const userInfo = await authService.getCurrentUser();

					set({
						user: userInfo,
						isAuthenticated: true,
						isLoading: false,
						error: null,
					});

					// Update role in role store
					if (setCurrentRole) {
						setCurrentRole(userInfo.role as UserRole);
					}
				} catch (error) {
					set({
						isLoading: false,
						error:
							error instanceof Error ? error.message : "Registration failed",
						isAuthenticated: false,
						user: null,
					});

					// Clear role in role store
					if (setCurrentRole) {
						setCurrentRole(null);
					}
				}
			},

			// Logout action
			logout: async () => {
				try {
					// 调用后端注销接口清除 Cookie
					await authService.logout();
				} catch (error) {
					console.error("Logout failed", error);
				} finally {
					set({
						isAuthenticated: false,
						user: null,
						error: null,
					});

					// Clear role in role store
					if (setCurrentRole) {
						setCurrentRole(null);
					}
				}
			},

			// Check authentication status
			checkAuth: async () => {
				try {
					set({ isLoading: true });
					// 通过 authService.getCurrentUser 接口获取当前用户信息
					const userInfo = await authService.getCurrentUser();

					set({
						user: userInfo,
						isAuthenticated: true,
						isLoading: false,
						error: null,
					});

					// Update role in role store
					if (setCurrentRole) {
						setCurrentRole(userInfo.role as UserRole);
					}
				} catch (error) {
					// 不显示错误信息，因为这是自动检查
					console.debug("Auth check failed:", error);

					// 如果未认证或发生错误，清空状态
					set({
						isAuthenticated: false,
						user: null,
						isLoading: false,
						error: null, // 不设置错误消息
					});

					// Clear role in role store
					if (setCurrentRole) {
						setCurrentRole(null);
					}
				}
			},

			// Set user
			setUser: (user: User | null) => {
				set({
					user,
					isAuthenticated: !!user,
				});

				// Update role in role store when user is updated
				if (setCurrentRole) {
					setCurrentRole(user?.role as UserRole | null);
				}
			},

			// Clear error
			clearError: () => {
				set({ error: null });
			},
		}),
		{
			name: "auth-storage", // Local storage key
			partialize: (state) => ({
				user: state.user,
				isAuthenticated: state.isAuthenticated,
			}),
		},
	),
);

// Helper function for API client (always returns null since we use HTTP-only cookies)
export const getAuthToken = (): string | null => {
	return null;
};

// Helper function to get current user
export const getCurrentUser = (): User | null => {
	return useAuthStore.getState().user;
};

// Simple authentication hook
export function useAuth() {
	const {
		isAuthenticated,
		user,
		login,
		logout,
		register,
		checkAuth,
		isLoading,
		error,
		clearError,
	} = useAuthStore();

	return {
		isAuthenticated,
		user,
		login,
		logout,
		register,
		checkAuth,
		isLoading,
		error,
		clearError,
	};
}
