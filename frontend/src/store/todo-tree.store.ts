import { create } from "zustand";

/**
 * TodoTree UI 狀態存儲 - 專門用於管理 TodoTree 組件的 UI 狀態
 * 將 UI 狀態與業務邏輯分離
 */

// 用於選擇項目的類型
export type SelectedItemType = "folder" | "todo" | null;

// 用於添加 Todo 的模態框數據類型
interface AddTodoModalData {
	isOpen: boolean;
	folderId?: number;
	folderName?: string;
}

// 用於刪除 Todo 的模態框數據類型
interface DeleteTodoModalData {
	isOpen: boolean;
	todoId: number | null;
}

// 用於創建文件夾的模態框數據類型
interface CreateFolderModalData {
	isOpen: boolean;
}

// 用於禁用 Todo 的模態框數據類型
interface BanTodoModalData {
	isOpen: boolean;
	todoId: number | null;
}

interface TodoTreeState {
	// 選擇狀態
	selectedUserId: number | null;
	selectedItemId: number | null;
	selectedItemType: SelectedItemType;

	// 展開的文件夾 ID 數組
	expandedFolders: number[];

	// 展示未分類待辦事項
	showUncategorizedTodos: boolean;

	// 模態框狀態
	addTodoModal: AddTodoModalData;
	deleteTodoModal: DeleteTodoModalData;
	createFolderModal: CreateFolderModalData;
	banTodoModal: BanTodoModalData;

	// 選擇操作
	setSelectedUser: (userId: number | null) => void;
	clearSelectedUser: () => void;
	setSelectedItem: (id: number | null, type: SelectedItemType) => void;

	// 操作方法 - 文件夾和 Todo 顯示
	toggleExpandFolder: (folderId: number) => void;
	toggleUncategorizedTodos: () => void;

	// 模態框操作 - 添加 Todo
	openAddTodoModal: (folderId?: number, folderName?: string) => void;
	closeAddTodoModal: () => void;

	// 模態框操作 - 刪除 Todo
	openDeleteTodoModal: (todoId: number) => void;
	closeDeleteTodoModal: () => void;

	// 模態框操作 - 創建文件夾
	openCreateFolderModal: () => void;
	closeCreateFolderModal: () => void;

	// 模態框操作 - 禁用 Todo
	openBanTodoModal: (todoId: number) => void;
	closeBanTodoModal: () => void;

	// 重置所有狀態
	resetState: () => void;
}

export const useTodoTreeStore = create<TodoTreeState>((set) => ({
	// 選擇狀態
	selectedUserId: null,
	selectedItemId: null,
	selectedItemType: null,

	// 初始狀態
	expandedFolders: [],
	showUncategorizedTodos: true,

	// 模態框初始狀態
	addTodoModal: {
		isOpen: false,
		folderId: undefined,
		folderName: undefined,
	},
	deleteTodoModal: {
		isOpen: false,
		todoId: null,
	},
	createFolderModal: {
		isOpen: false,
	},
	banTodoModal: {
		isOpen: false,
		todoId: null,
	},

	// 選擇操作
	setSelectedUser: (userId: number | null) => {
		console.log("TodoTreeStore: Setting selectedUserId to", userId);
		set({ selectedUserId: userId });
	},
	clearSelectedUser: () => {
		console.log("TodoTreeStore: Clearing selectedUserId");
		set({ selectedUserId: null });
	},

	// 選擇項目
	setSelectedItem: (id: number | null, type: SelectedItemType) => {
		console.log(`TodoTreeStore: Setting selectedItem to ${type} with id ${id}`);
		set({
			selectedItemId: id,
			selectedItemType: type,
		});
	},

	// 切換文件夾展開狀態
	toggleExpandFolder: (folderId: number) => {
		set((state) => ({
			expandedFolders: state.expandedFolders.includes(folderId)
				? state.expandedFolders.filter((id) => id !== folderId)
				: [...state.expandedFolders, folderId],
		}));
	},

	// 切換未分類待辦事項顯示狀態
	toggleUncategorizedTodos: () => {
		set((state) => ({
			showUncategorizedTodos: !state.showUncategorizedTodos,
		}));
	},

	// 打開添加 Todo 模態框
	openAddTodoModal: (folderId?: number, folderName?: string) => {
		set({
			addTodoModal: {
				isOpen: true,
				folderId,
				folderName,
			},
		});
	},

	// 關閉添加 Todo 模態框
	closeAddTodoModal: () => {
		set({
			addTodoModal: {
				isOpen: false,
				folderId: undefined,
				folderName: undefined,
			},
		});
	},

	// 打開刪除 Todo 模態框
	openDeleteTodoModal: (todoId: number) => {
		set({
			deleteTodoModal: {
				isOpen: true,
				todoId,
			},
		});
	},

	// 關閉刪除 Todo 模態框
	closeDeleteTodoModal: () => {
		set({
			deleteTodoModal: {
				isOpen: false,
				todoId: null,
			},
		});
	},

	// 打開創建文件夾模態框
	openCreateFolderModal: () => {
		set({
			createFolderModal: {
				isOpen: true,
			},
		});
	},

	// 關閉創建文件夾模態框
	closeCreateFolderModal: () => {
		set({
			createFolderModal: {
				isOpen: false,
			},
		});
	},

	// 打開禁用 Todo 模態框
	openBanTodoModal: (todoId: number) => {
		set({
			banTodoModal: {
				isOpen: true,
				todoId,
			},
		});
	},

	// 關閉禁用 Todo 模態框
	closeBanTodoModal: () => {
		set({
			banTodoModal: {
				isOpen: false,
				todoId: null,
			},
		});
	},

	// 重置狀態
	resetState: () => {
		set({
			selectedUserId: null,
			selectedItemId: null,
			selectedItemType: null,
			expandedFolders: [],
			showUncategorizedTodos: true,
			addTodoModal: {
				isOpen: false,
				folderId: undefined,
				folderName: undefined,
			},
			deleteTodoModal: {
				isOpen: false,
				todoId: null,
			},
			createFolderModal: {
				isOpen: false,
			},
			banTodoModal: {
				isOpen: false,
				todoId: null,
			},
		});
	},
}));
