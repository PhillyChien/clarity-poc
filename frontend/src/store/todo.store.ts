import { create } from "zustand";
import { todoService, usersService } from "../services/backend";
import type {
	CreateTodoRequest,
	Todo,
	UpdateTodoRequest,
} from "../services/backend/types";
import { useTodoTreeStore } from "./todo-tree.store";

interface TodoState {
	// todos by folder id
	todosByFolder: Record<number, Todo[]>;
	// uncategorized todos
	uncategorizedTodos: Todo[];
	// current selected folder id
	currentFolderId: number | null;
	isLoading: boolean;
	error: string | null;

	// Getters
	getCurrentTodos: () => Todo[];

	// Actions
	fetchTodosByFolder: (folderId: number) => Promise<void>;
	fetchUncategorizedTodos: () => Promise<void>;
	fetchTodosByUserId: (userId: number) => Promise<void>;
	fetchUserTodos: () => Promise<void>;
	getTodo: (todoId: number) => Promise<Todo | undefined>;
	addTodo: (todoData: CreateTodoRequest) => Promise<Todo | undefined>;
	updateTodo: (
		todoId: number,
		todoData: UpdateTodoRequest,
	) => Promise<Todo | undefined>;
	toggleTodoCompletion: (todoId: number) => Promise<Todo | undefined>;
	deleteTodo: (todoId: number) => Promise<void>;
	clearError: () => void;
	resetStore: () => void;
	disableTodo: (todoId: number) => Promise<void>;
}

// helper function: get current folder or uncategorized todos
const getCurrentTodos = (state: TodoState): Todo[] => {
	const { todosByFolder, currentFolderId, uncategorizedTodos } = state;
	return currentFolderId
		? todosByFolder[currentFolderId] || []
		: uncategorizedTodos;
};

export const useTodoStore = create<TodoState>((set, get) => ({
	todosByFolder: {},
	uncategorizedTodos: [],
	currentFolderId: null,
	isLoading: false,
	error: null,

	// get current folder todos
	getCurrentTodos: () => {
		const { todosByFolder, currentFolderId, uncategorizedTodos } = get();
		return currentFolderId
			? todosByFolder[currentFolderId] || []
			: uncategorizedTodos;
	},

	// fetch uncategorized todos
	fetchUncategorizedTodos: async () => {
		try {
			set({ isLoading: true, error: null });

			// 从服务器获取所有待办事项
			const allTodos = await todoService.getUserTodos();

			// 过滤出未关联到文件夹的待办事项
			const uncategorized = allTodos.filter(
				(todo: Todo) => todo.folderId === null || todo.folderId === undefined,
			);

			console.log("找到未分类的待办事项:", uncategorized.length);
			set({
				uncategorizedTodos: uncategorized,
				isLoading: false,
			});
		} catch (error: unknown) {
			console.error("加载未分类待办事项失败:", error);
			set({
				isLoading: false,
				error:
					error instanceof Error
						? error.message
						: "Failed to get uncategorized todos",
			});
		}
	},

	// 获取指定用户的所有待办事项 (版主功能)
	fetchTodosByUserId: async (userId: number) => {
		try {
			set({ isLoading: true, error: null });

			// 从 usersService 获取指定用户的所有待办事项
			const todos = await usersService.getUserTodos(userId);

			// 按照文件夹ID分组待办事项
			const todosByFolder: Record<number, Todo[]> = {};
			const uncategorized: Todo[] = [];

			for (const todo of todos) {
				if (todo.folderId) {
					if (!todosByFolder[todo.folderId]) {
						todosByFolder[todo.folderId] = [];
					}
					todosByFolder[todo.folderId].push(todo);
				} else {
					uncategorized.push(todo);
				}
			}

			// 更新状态
			set({
				todosByFolder,
				uncategorizedTodos: uncategorized,
				isLoading: false,
			});
		} catch (error: unknown) {
			console.error(`Error fetching todos for user ${userId}:`, error);
			set({
				todosByFolder: {},
				uncategorizedTodos: [],
				isLoading: false,
				error: error instanceof Error ? error.message : "获取用户待办事项失败",
			});
		}
	},

	// 获取文件夹中的待办事项
	fetchTodosByFolder: async (folderId: number) => {
		try {
			set({ isLoading: true, error: null, currentFolderId: folderId });

			// 检查是否已缓存该文件夹的待办事项
			const cachedTodos = get().todosByFolder[folderId];
			if (cachedTodos) {
				console.log(`Using cached todos for folder ${folderId}:`, cachedTodos);
				set({ isLoading: false });
				return;
			}

			// 如果未缓存，则从服务器获取
			console.log(`Fetching todos for folder ${folderId} from server`);
			const todos = await todoService.getTodosByFolder(folderId);
			console.log(`Received todos for folder ${folderId} from server:`, todos);

			// 更新缓存
			set((state) => {
				const newState = {
					todosByFolder: {
						...state.todosByFolder,
						[folderId]: todos,
					},
					isLoading: false,
				};
				console.log(`Updated state for folder ${folderId}:`, newState);
				return newState;
			});
		} catch (error: unknown) {
			console.error(`Error fetching todos for folder ${folderId}:`, error);
			set({
				isLoading: false,
				error:
					error instanceof Error ? error.message : "Failed to get folder todos",
			});
		}
	},

	// getTodo fetch todo by id
	getTodo: async (todoId: number) => {
		try {
			set({ isLoading: true, error: null });

			// first check in current folder
			const state = get();
			const currentTodos = getCurrentTodos(state);
			let todo = currentTodos.find((t) => t.id === todoId);

			// then check in all cached folders
			if (!todo) {
				for (const folderTodos of Object.values(state.todosByFolder)) {
					todo = folderTodos.find((t) => t.id === todoId);
					if (todo) break;
				}
			}

			// check uncategorized list
			if (!todo) {
				todo = state.uncategorizedTodos.find((t) => t.id === todoId);
			}

			// if still not found, fetch from server
			if (!todo) {
				todo = await todoService.getTodo(todoId);

				// if todo has folderId, add to corresponding cache
				if (todo?.folderId) {
					const folderId = todo.folderId;
					set((state) => ({
						todosByFolder: {
							...state.todosByFolder,
							[folderId]: [
								...(state.todosByFolder[folderId] || []),
								todo as Todo,
							],
						},
					}));
				} else if (todo) {
					// if todo is uncategorized, add to uncategorized list
					set((state) => ({
						uncategorizedTodos: [...state.uncategorizedTodos, todo as Todo],
					}));
				}
			}

			set({ isLoading: false });
			return todo;
		} catch (error: unknown) {
			set({
				isLoading: false,
				error:
					error instanceof Error ? error.message : "Failed to get todo details",
			});
			return undefined;
		}
	},

	// addTodo add new todo
	addTodo: async (todoData: CreateTodoRequest) => {
		try {
			set({ isLoading: true, error: null });
			const newTodo = await todoService.createTodo(todoData);

			// update cache
			set((state) => {
				// create a copy of the update function
				const newState = { ...state, isLoading: false };

				// if todo has folderId, add to corresponding cache
				if (newTodo.folderId) {
					const folderId = newTodo.folderId;
					newState.todosByFolder = {
						...state.todosByFolder,
						[folderId]: [...(state.todosByFolder[folderId] || []), newTodo],
					};
				} else {
					// if todo is uncategorized, update uncategorized list
					newState.uncategorizedTodos = [...state.uncategorizedTodos, newTodo];
				}

				return newState;
			});

			return newTodo;
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "Failed to create todo",
			});
			return undefined;
		}
	},

	// updateTodo update todo
	updateTodo: async (todoId: number, todoData: UpdateTodoRequest) => {
		try {
			set({ isLoading: true, error: null });
			const updatedTodo = await todoService.updateTodo(todoId, todoData);

			set((state) => {
				const newState = { ...state, isLoading: false };

				// find todo in all folders to get old folderId
				let oldFolderId: number | null = null;
				for (const [folderId, todos] of Object.entries(state.todosByFolder)) {
					const todo = todos.find((t) => t.id === todoId);
					if (todo) {
						oldFolderId = Number(folderId);
						break;
					}
				}

				// check uncategorized list
				const isInUncategorized = state.uncategorizedTodos.some(
					(t) => t.id === todoId,
				);

				const newFolderId = updatedTodo.folderId;

				// if folder changed, update two lists
				if (oldFolderId !== newFolderId) {
					// remove from old folder
					if (oldFolderId) {
						newState.todosByFolder = {
							...state.todosByFolder,
							[oldFolderId]:
								state.todosByFolder[oldFolderId]?.filter(
									(t) => t.id !== todoId,
								) || [],
						};
					}

					// add to new folder
					if (newFolderId) {
						newState.todosByFolder = {
							...newState.todosByFolder,
							[newFolderId]: [
								...(state.todosByFolder[newFolderId] || []),
								updatedTodo,
							],
						};
					}
				} else if (newFolderId) {
					// if folder not changed, update only corresponding folder
					newState.todosByFolder = {
						...state.todosByFolder,
						[newFolderId]:
							state.todosByFolder[newFolderId]?.map((t) =>
								t.id === todoId ? updatedTodo : t,
							) || [],
					};
				}

				// 更新未分类列表
				if (isInUncategorized && newFolderId) {
					// 如果从未分类移到文件夹，从未分类列表中移除
					newState.uncategorizedTodos = state.uncategorizedTodos.filter(
						(t) => t.id !== todoId,
					);
				} else if (!isInUncategorized && !newFolderId) {
					// 如果从文件夹移到未分类，添加到未分类列表
					newState.uncategorizedTodos = [
						...state.uncategorizedTodos,
						updatedTodo,
					];
				} else if (isInUncategorized && !newFolderId) {
					// 如果在未分类列表中更新，更新该项
					newState.uncategorizedTodos = state.uncategorizedTodos.map((t) =>
						t.id === todoId ? updatedTodo : t,
					);
				}

				return newState;
			});

			return updatedTodo;
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "Failed to update todo",
			});
			return undefined;
		}
	},

	// toggleTodoCompletion toggle todo completion
	toggleTodoCompletion: async (todoId: number) => {
		try {
			set({ isLoading: true, error: null });

			// find todo location
			const state = get();
			let existingTodo: Todo | undefined;
			let foundInFolder: number | null = null;
			let foundInUncategorized = false;

			// check current folder
			if (state.currentFolderId) {
				existingTodo = state.todosByFolder[state.currentFolderId]?.find(
					(t) => t.id === todoId,
				);
				if (existingTodo) {
					foundInFolder = state.currentFolderId;
				}
			}

			// check all folders
			if (!existingTodo) {
				for (const [folderIdStr, todos] of Object.entries(
					state.todosByFolder,
				)) {
					existingTodo = todos.find((t) => t.id === todoId);
					if (existingTodo) {
						foundInFolder = Number(folderIdStr);
						break;
					}
				}
			}

			// check uncategorized list
			if (!existingTodo) {
				existingTodo = state.uncategorizedTodos.find((t) => t.id === todoId);
				if (existingTodo) {
					foundInUncategorized = true;
				}
			}

			console.log(
				`[Toggle] Found todo: ${existingTodo?.title}, completed: ${existingTodo?.completed}`,
			);

			// toggle todo completion on server
			const updatedTodo = await todoService.toggleTodoCompletion(todoId);
			console.log(
				`[Toggle] API response: ${updatedTodo?.title}, completed: ${updatedTodo?.completed}`,
			);

			// update local state
			set((state) => {
				const newState = { ...state, isLoading: false };

				// update in all possible locations to ensure UI is consistent
				// 1. update in folder if found there
				if (foundInFolder !== null) {
					newState.todosByFolder = {
						...state.todosByFolder,
						[foundInFolder]:
							state.todosByFolder[foundInFolder]?.map((t) =>
								t.id === todoId
									? {
											...t,
											completed: !t.completed, // ensure toggled locally
										}
									: t,
							) || [],
					};
				}

				// 2. update in uncategorized list if found there
				if (foundInUncategorized || !updatedTodo.folderId) {
					newState.uncategorizedTodos = state.uncategorizedTodos.map((t) =>
						t.id === todoId
							? {
									...t,
									completed: !t.completed, // ensure toggled locally
								}
							: t,
					);
				}

				// 3. if not found anywhere but we have updated todo, make sure it's in the right place
				if (!foundInFolder && !foundInUncategorized) {
					if (updatedTodo.folderId) {
						// add to folder
						const folderId = updatedTodo.folderId;
						newState.todosByFolder = {
							...state.todosByFolder,
							[folderId]: [
								...(state.todosByFolder[folderId] || []),
								updatedTodo,
							],
						};
					} else {
						// add to uncategorized
						newState.uncategorizedTodos = [
							...state.uncategorizedTodos,
							updatedTodo,
						];
					}
				}

				return newState;
			});

			return updatedTodo;
		} catch (error: unknown) {
			console.error("Error toggling todo completion:", error);
			set({
				isLoading: false,
				error:
					error instanceof Error
						? error.message
						: "Failed to toggle todo status",
			});
			return undefined;
		}
	},

	// deleteTodo delete todo
	deleteTodo: async (todoId: number) => {
		try {
			set({ isLoading: true, error: null });

			// find todo location
			const state = get();
			let folderId: number | null = null;
			let isInFolder = false;
			let isInUncategorized = false;

			// find todo location
			for (const [folderIdStr, todos] of Object.entries(state.todosByFolder)) {
				const todo = todos.find((t) => t.id === todoId);
				if (todo) {
					folderId = Number(folderIdStr);
					isInFolder = true;
					break;
				}
			}

			// check if in uncategorized list
			if (!isInFolder) {
				isInUncategorized = state.uncategorizedTodos.some(
					(t) => t.id === todoId,
				);
			}

			// delete todo on server
			await todoService.deleteTodo(todoId);

			// update local state
			set((state) => {
				const newState = { ...state, isLoading: false };

				if (isInFolder && folderId !== null) {
					newState.todosByFolder = {
						...state.todosByFolder,
						[folderId]:
							state.todosByFolder[folderId]?.filter((t) => t.id !== todoId) ||
							[],
					};
				}

				if (isInUncategorized) {
					newState.uncategorizedTodos = state.uncategorizedTodos.filter(
						(t) => t.id !== todoId,
					);
				}

				return newState;
			});

			// 检查当前选中的项目是否为刚删除的 todo，如果是则清除选择状态
			const todoTreeStore = useTodoTreeStore.getState();
			if (
				todoTreeStore.selectedItemId === todoId &&
				todoTreeStore.selectedItemType === "todo"
			) {
				console.log(`清除已删除的 Todo ${todoId} 的选择状态`);
				useTodoTreeStore.setState({
					selectedItemId: null,
					selectedItemType: null,
				});
			}
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "Failed to delete todo",
			});
		}
	},

	// clearError clear error
	clearError: () => {
		set({ error: null });
	},

	// resetStore reset the store
	resetStore: () => {
		set({
			todosByFolder: {},
			uncategorizedTodos: [],
			currentFolderId: null,
			isLoading: false,
			error: null,
		});
	},

	// disableTodo - 禁用或启用待办事项 (由版主调用)
	disableTodo: async (todoId: number) => {
		try {
			set({ isLoading: true, error: null });

			// 调用待办事项服务的toggleTodoStatus
			const response = await usersService.toggleTodoStatus(todoId);
			console.log(`Todo status toggled successfully: ${response.message}`);

			// 更新状态 - 反转禁用状态
			set((state) => {
				// 在所有地方查找待办事项
				const newState = { ...state, isLoading: false };
				let foundAndUpdated = false;

				// 更新文件夹中的待办事项
				for (const folderId in newState.todosByFolder) {
					const folderTodos = newState.todosByFolder[folderId];
					const todoIndex = folderTodos.findIndex((t) => t.id === todoId);

					if (todoIndex !== -1) {
						// 找到后反转禁用状态
						newState.todosByFolder[folderId] = [
							...folderTodos.slice(0, todoIndex),
							{
								...folderTodos[todoIndex],
								disabled: !folderTodos[todoIndex].disabled,
							},
							...folderTodos.slice(todoIndex + 1),
						];
						foundAndUpdated = true;
						break;
					}
				}

				// 检查未分类的待办事项
				if (!foundAndUpdated) {
					const uncatIndex = newState.uncategorizedTodos.findIndex(
						(t) => t.id === todoId,
					);
					if (uncatIndex !== -1) {
						newState.uncategorizedTodos = [
							...newState.uncategorizedTodos.slice(0, uncatIndex),
							{
								...newState.uncategorizedTodos[uncatIndex],
								disabled: !newState.uncategorizedTodos[uncatIndex].disabled,
							},
							...newState.uncategorizedTodos.slice(uncatIndex + 1),
						];
					}
				}

				return newState;
			});

			// 选中项目发生变化后，通知TodoTreeStore以更新UI
			const { selectedItemId, selectedItemType } = useTodoTreeStore.getState();
			if (selectedItemId === todoId && selectedItemType === "todo") {
				// 重新选中该项目以刷新详情视图
				useTodoTreeStore.getState().setSelectedItem(todoId, "todo");
			}
		} catch (error: unknown) {
			console.error("Failed to toggle todo disabled status:", error);
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "禁用待办事项失败",
			});
		}
	},

	// 获取当前用户的所有待办事项
	fetchUserTodos: async () => {
		try {
			set({ isLoading: true, error: null });

			// 从服务器获取当前用户的所有待办事项
			const todos = await todoService.getUserTodos();

			// 按照文件夹ID分组待办事项
			const todosByFolder: Record<number, Todo[]> = {};
			const uncategorized: Todo[] = [];

			for (const todo of todos) {
				if (todo.folderId) {
					if (!todosByFolder[todo.folderId]) {
						todosByFolder[todo.folderId] = [];
					}
					todosByFolder[todo.folderId].push(todo);
				} else {
					uncategorized.push(todo);
				}
			}

			// 更新状态
			set({
				todosByFolder,
				uncategorizedTodos: uncategorized,
				isLoading: false,
			});
		} catch (error: unknown) {
			console.error("Error fetching user todos:", error);
			set({
				isLoading: false,
				error:
					error instanceof Error ? error.message : "Failed to get user todos",
			});
		}
	},
}));
