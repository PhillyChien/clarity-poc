import { create } from "zustand";
import { todoService } from "../services/backend";
import type {
	CreateTodoRequest,
	Todo,
	UpdateTodoRequest,
} from "../services/backend/types";

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
	getTodo: (todoId: number) => Promise<Todo | undefined>;
	addTodo: (todoData: CreateTodoRequest) => Promise<Todo | undefined>;
	updateTodo: (
		todoId: number,
		todoData: UpdateTodoRequest,
	) => Promise<Todo | undefined>;
	toggleTodoCompletion: (todoId: number) => Promise<Todo | undefined>;
	deleteTodo: (todoId: number) => Promise<void>;
	clearError: () => void;
}

export const useTodoStore = create<TodoState>((set, get) => ({
	todosByFolder: {},
	uncategorizedTodos: [],
	currentFolderId: null,
	isLoading: false,
	error: null,

	// 获取当前文件夹的待办事项
	getCurrentTodos: () => {
		const { todosByFolder, currentFolderId } = get();
		return currentFolderId ? todosByFolder[currentFolderId] || [] : [];
	},

	// 获取未分类的待办事项
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
			set({ uncategorizedTodos: uncategorized, isLoading: false });
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
			const currentTodos = state.getCurrentTodos();
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
					get().fetchUncategorizedTodos();
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

						// if was uncategorized, now has folder, update uncategorized list
						if (isInUncategorized) {
							get().fetchUncategorizedTodos();
						}
					} else {
						// if was uncategorized, now has folder, update uncategorized list
						get().fetchUncategorizedTodos();
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
				} else if (isInUncategorized) {
					// if uncategorized todo updated, update uncategorized list
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
				for (const [folderIdStr, todos] of Object.entries(state.todosByFolder)) {
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

			console.log(`[Toggle] Found todo: ${existingTodo?.title}, completed: ${existingTodo?.completed}`);

			// toggle todo completion on server
			const updatedTodo = await todoService.toggleTodoCompletion(todoId);
			console.log(`[Toggle] API response: ${updatedTodo?.title}, completed: ${updatedTodo?.completed}`);

			// update local state
			set((state) => {
				const newState = { ...state, isLoading: false };

				// update in all possible locations to ensure UI is consistent
				// 1. update in folder if found there
				if (foundInFolder !== null) {
					newState.todosByFolder = {
						...state.todosByFolder,
						[foundInFolder]: state.todosByFolder[foundInFolder]?.map((t) =>
							t.id === todoId ? {
								...t,
								completed: !t.completed // ensure toggled locally
							} : t,
						) || [],
					};
				}

				// 2. update in uncategorized list if found there
				if (foundInUncategorized || !updatedTodo.folderId) {
					newState.uncategorizedTodos = state.uncategorizedTodos.map((t) =>
						t.id === todoId ? {
							...t,
							completed: !t.completed // ensure toggled locally
						} : t,
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
								updatedTodo
							]
						};
					} else {
						// add to uncategorized
						newState.uncategorizedTodos = [
							...state.uncategorizedTodos,
							updatedTodo
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
			if (isInFolder && folderId !== null) {
				set((state) => ({
					todosByFolder: {
						...state.todosByFolder,
						[folderId as number]:
							state.todosByFolder[folderId as number]?.filter(
								(t) => t.id !== todoId,
							) || [],
					},
					isLoading: false,
				}));
			} else if (isInUncategorized) {
				set((state) => ({
					uncategorizedTodos: state.uncategorizedTodos.filter(
						(t) => t.id !== todoId,
					),
					isLoading: false,
				}));
			} else {
				// if not found in local state, refresh cache
				set({ isLoading: false });

				// if current has selected folder, refresh folder todos
				if (state.currentFolderId) {
					get().fetchTodosByFolder(state.currentFolderId);
				}

				// refresh uncategorized todos
				get().fetchUncategorizedTodos();
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
}));
