"use client";

import { adminService } from "@/services/backend/adminService";
import type { MessageResponse, UserResponse } from "@/services/backend/types";
import { create } from "zustand";

interface AdminState {
	users: UserResponse[];
	isLoading: boolean;
	error: string | null;

	// Actions
	fetchAllUsers: () => Promise<void>;
	updateUserRole: (userId: number, role: string) => Promise<MessageResponse | undefined>;
	clearError: () => void;
}

export const useAdminStore = create<AdminState>((set) => ({
	users: [],
	isLoading: false,
	error: null,

	// 获取所有用户
	fetchAllUsers: async () => {
		try {
			set({ isLoading: true, error: null });
			const users = await adminService.getAllUsers();
			set({ users, isLoading: false });
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "获取用户列表失败",
			});
			return undefined;
		}
	},

	// 更新用户角色
	updateUserRole: async (userId: number, role: string) => {
		try {
			set({ isLoading: true, error: null });
			const response = await adminService.updateUserRole(userId, role);
			set({ isLoading: false });
			
			// 更新本地用户列表以反映角色变化
			set((state) => ({
				users: state.users.map((user) => 
					user.id === userId ? { ...user, role } : user
				)
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

	// 清除错误
	clearError: () => {
		set({ error: null });
	},
})); 