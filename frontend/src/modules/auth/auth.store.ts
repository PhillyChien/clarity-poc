import { create } from "zustand";
import { persist } from "zustand/middleware";
import { authService } from "../../services/backend";
import type { User, UserRole, Permission } from "../../services/backend/types";

// Forward declare role store functions to avoid circular dependency
let setCurrentRole: ((role: UserRole | null) => void) | null = null;
let setCurrentPermissions: ((permissions: Permission[]) => void) | null = null;

// Function to be called by role store to set its setCurrentRole function
export const registerRoleStoreSetCurrentRole = (
	fn: (role: UserRole | null) => void,
) => {
	setCurrentRole = fn;
};

// Function to be called by role store to set its setCurrentPermissions function
export const registerRoleStoreSetCurrentPermissions = (
	fn: (permissions: Permission[]) => void,
) => {
	setCurrentPermissions = fn;
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

					// Call the login API first
					await authService.login({ username, password });

					// Get user information
					const userInfo = await authService.getCurrentUser();

					set({
						user: userInfo,
						isAuthenticated: true,
						isLoading: false,
						error: null,
					});

					// Update role in role store
					if (setCurrentRole) {
						setCurrentRole(userInfo.role);
					}
					
					// Update the permissions list in the role store
					if (setCurrentPermissions && userInfo.permissions) {
						// Use the type conversion function to convert the string array to the Permission array
						setCurrentPermissions(userInfo.permissions);
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
					
					// 清空权限列表
					if (setCurrentPermissions) {
						setCurrentPermissions([]);
					}
				}
			},

			// Register action
			register: async (username: string, email: string, password: string) => {
				try {
					set({ isLoading: true, error: null });

					// Call the registration interface first
					await authService.register({ username, email, password });

					// Get user information after successful registration
					const userInfo = await authService.getCurrentUser();

					set({
						user: userInfo,
						isAuthenticated: true,
						isLoading: false,
						error: null,
					});

					// Update role in role store
					if (setCurrentRole) {
						setCurrentRole(userInfo.role);
					}
					
					// Update the permissions list in the role store
					if (setCurrentPermissions && userInfo.permissions) {
						// Use the type conversion function to convert the string array to the Permission array
						setCurrentPermissions(userInfo.permissions);
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
					
					// Clear the permissions list
					if (setCurrentPermissions) {
						setCurrentPermissions([]);
					}
				}
			},

			// Logout action
			logout: async () => {
				try {
					// Call the backend logout interface to clear the Cookie
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
					
					// Clear the permissions list
					if (setCurrentPermissions) {
						setCurrentPermissions([]);
					}
				}
			},

			// Check authentication status
			checkAuth: async () => {
				try {
					set({ isLoading: true });
					// Get the current user information through the authService.getCurrentUser interface
					const userInfo = await authService.getCurrentUser();

					set({
						user: userInfo,
						isAuthenticated: true,
						isLoading: false,
						error: null,
					});

					// Update role in role store
					if (setCurrentRole) {
						setCurrentRole(userInfo.role);
					}
					
					// Update the permissions list in the role store
					if (setCurrentPermissions && userInfo.permissions) {
						// Use the type conversion function to convert the string array to the Permission array
						setCurrentPermissions(userInfo.permissions);
					}
				} catch (error) {
					// Do not display error information, because this is an automatic check
					console.debug("Auth check failed:", error);

					// If not authenticated or an error occurs, clear the status
					set({
						isAuthenticated: false,
						user: null,
						isLoading: false,
						error: null, // Do not set an error message
					});

					// Clear role in role store
					if (setCurrentRole) {
						setCurrentRole(null);
					}
					
					// Clear the permissions list
					if (setCurrentPermissions) {
						setCurrentPermissions([]);
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
					setCurrentRole(user?.role || null);
				}
				
				// Update the permissions list in the role store
				if (setCurrentPermissions) {
					if (user && user.permissions) {
						// Use the type conversion function to convert the string array to the Permission array
						setCurrentPermissions(user.permissions);
					} else {
						setCurrentPermissions([]);
					}
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
