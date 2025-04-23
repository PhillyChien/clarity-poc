"use client";

import type { MessageResponse, UserResponse, UserRole } from "@/services/backend/types";
import { usersService } from "@/services/backend/usersService";
import { create } from "zustand";

interface UsersState {
	users: UserResponse[];
	isLoading: boolean;
	error: string | null;

	// Actions
	fetchAllUsers: () => Promise<void>;
	updateUserRole: (
		userId: number,
		role: UserRole,
	) => Promise<MessageResponse | undefined>;
	disableTodo: (todoId: number) => Promise<void>;
	enableTodo: (todoId: number) => Promise<void>;
	clearError: () => void;
}

export const useUsersStore = create<UsersState>((set) => ({
	users: [],
	isLoading: false,
	error: null,

	// 获取所有用户
	fetchAllUsers: async () => {
		try {
			set({ isLoading: true, error: null });
			const users = await usersService.getAllUsers();
			set({ users, isLoading: false });
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "获取用户列表失败",
			});
		}
	},

	// 更新用户角色
	updateUserRole: async (userId: number, role: UserRole) => {
		try {
			set({ isLoading: true, error: null });
			const response = await usersService.updateUserRole(userId, role);
			set({ isLoading: false });

			// 更新本地用户列表以反映角色变化
			set((state) => ({
				users: state.users.map((user) =>
					user.id === userId ? { ...user, role } : user,
				),
			}));

			return response;
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "更新用户角色失败",
			});
			return undefined;
		}
	},

	// 禁用待办事项
	disableTodo: async (todoId: number) => {
		try {
			set({ isLoading: true, error: null });
			await usersService.toggleTodoStatus(todoId);
			set({ isLoading: false });
		} catch (error: unknown) {
			set({
				isLoading: false,
				error:
					error instanceof Error ? error.message : "Failed to disable todo",
			});
		}
	},

	// 启用待办事项
	enableTodo: async (todoId: number) => {
		try {
			set({ isLoading: true, error: null });
			await usersService.toggleTodoStatus(todoId);
			set({ isLoading: false });
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "Failed to enable todo",
			});
		}
	},

	// 清除错误
	clearError: () => {
		set({ error: null });
	},
}));
