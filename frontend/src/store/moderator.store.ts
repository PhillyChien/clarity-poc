"use client";

import { moderatorService } from "@/services/backend/moderatorService";
import type { UserResponse } from "@/services/backend/types";
import { create } from "zustand";

interface ModeratorState {
	users: UserResponse[];
	isLoading: boolean;
	error: string | null;

	// Actions
	fetchAllUsers: () => Promise<void>;
	disableTodo: (todoId: number) => Promise<void>;
	enableTodo: (todoId: number) => Promise<void>;
	clearError: () => void;
}

export const useModeratorStore = create<ModeratorState>((set) => ({
	users: [],
	isLoading: false,
	error: null,

	// Fetch all users
	fetchAllUsers: async () => {
		try {
			set({ isLoading: true, error: null });
			const users = await moderatorService.getAllUsers();
			set({ users, isLoading: false });
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "Failed to fetch users",
			});
		}
	},

	// Disable a todo
	disableTodo: async (todoId: number) => {
		try {
			set({ isLoading: true, error: null });
			await moderatorService.disableTodo(todoId);
			set({ isLoading: false });
		} catch (error: unknown) {
			set({
				isLoading: false,
				error:
					error instanceof Error ? error.message : "Failed to disable todo",
			});
		}
	},

	// Enable a todo
	enableTodo: async (todoId: number) => {
		try {
			set({ isLoading: true, error: null });
			await moderatorService.enableTodo(todoId);
			set({ isLoading: false });
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "Failed to enable todo",
			});
		}
	},

	// Clear error
	clearError: () => {
		set({ error: null });
	},
}));
