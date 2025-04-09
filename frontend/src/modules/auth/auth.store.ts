import { create } from "zustand";
import { persist } from "zustand/middleware";
import { authService } from "../../services/backend";
import type { User } from "../../services/backend/types";
import { UserRole } from "./types";

// Forward declare role store to avoid circular dependency
let setCurrentRole: ((role: UserRole | null) => void) | null = null;

// Function to be called by role store to set its setCurrentRole function
export const registerRoleStoreSetCurrentRole = (fn: (role: UserRole | null) => void) => {
	setCurrentRole = fn;
};

interface AuthState {
	isAuthenticated: boolean;
	token: string | null;
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
	logout: () => void;
	setUser: (user: User | null) => void;
	clearError: () => void;
}

// Create authentication store
export const useAuthStore = create<AuthState>()(
	persist(
		(set) => ({
			isAuthenticated: false,
			token: null,
			user: null,
			isLoading: false,
			error: null,

			// Login action
			login: async (username: string, password: string) => {
				try {
					set({ isLoading: true, error: null });
					const response = await authService.login({ username, password });

					// Save token and user info
					const user = authService.buildUserFromJwtResponse(response);
					set({
						token: response.token,
						user: user,
						isAuthenticated: true,
						isLoading: false,
					});
					
					// Update role in role store
					if (setCurrentRole) {
						setCurrentRole(user.role as UserRole);
					}
				} catch (error) {
					set({
						isLoading: false,
						error: error instanceof Error ? error.message : "Login failed",
						isAuthenticated: false,
						token: null,
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
					// Register user
					await authService.register({ username, email, password });

					// Auto login after successful registration
					const loginResponse = await authService.login({ username, password });
					const user = authService.buildUserFromJwtResponse(loginResponse);

					set({
						token: loginResponse.token,
						user: user,
						isAuthenticated: true,
						isLoading: false,
					});
					
					// Update role in role store
					if (setCurrentRole) {
						setCurrentRole(user.role as UserRole);
					}
				} catch (error) {
					set({
						isLoading: false,
						error: error instanceof Error ? error.message : "Registration failed",
						isAuthenticated: false,
						token: null,
						user: null,
					});
					
					// Clear role in role store
					if (setCurrentRole) {
						setCurrentRole(null);
					}
				}
			},

			// Logout action
			logout: () => {
				set({
					isAuthenticated: false,
					token: null,
					user: null,
					error: null,
				});
				
				// Clear role in role store
				if (setCurrentRole) {
					setCurrentRole(null);
				}
			},

			// Set user
			setUser: (user: User | null) => {
				set({ user });
				
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
				token: state.token,
				user: state.user,
			}),
		},
	),
);

// Helper function to get token for apiClient
export const getAuthToken = (): string | null => {
	return useAuthStore.getState().token;
};

// Helper function to get current user
export const getCurrentUser = (): User | null => {
	return useAuthStore.getState().user;
};

// Simple authentication hook
export function useAuth() {
	const { isAuthenticated, user, login, logout, register, isLoading, error, clearError } = useAuthStore();

	return {
		isAuthenticated,
		user,
		login,
		logout,
		register,
		isLoading,
		error,
		clearError
	};
} 