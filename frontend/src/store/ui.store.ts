import { create } from "zustand";

/**
 * UI状态存储 - 管理与UI相关的纯前端状态
 * 不包含任何与API或数据模型直接相关的状态
 */
export type FolderSelection = number | null;

interface UIState {
	// 模态框状态
	isTodoDetailModalOpen: boolean;
	selectedTodoIdForDetail: number | null;
	isCreateFolderModalOpen: boolean;

	// 页面视图状态
	sidebarExpanded: boolean;
	mobileSidebarOpen: boolean;

	// 模态框操作
	openTodoDetailModal: (todoId: number) => void;
	closeTodoDetailModal: () => void;
	openCreateFolderModal: () => void;
	closeCreateFolderModal: () => void;

	// 侧边栏操作
	toggleSidebar: () => void;
	openMobileSidebar: () => void;
	closeMobileSidebar: () => void;

	selectedFolderId: FolderSelection;
	todoDetailId: number | null;

	// 设置选中的文件夹
	setSelectedFolder: (folderId: FolderSelection) => void;

	// 设置要查看详情的Todo ID
	setTodoDetailId: (todoId: number | null) => void;
}

export const useUIStore = create<UIState>((set) => ({
	// 模态框状态
	isTodoDetailModalOpen: false,
	selectedTodoIdForDetail: null,
	isCreateFolderModalOpen: false,

	// 页面视图状态
	sidebarExpanded: true,
	mobileSidebarOpen: false,

	// 模态框操作
	openTodoDetailModal: (todoId: number) => {
		set({
			isTodoDetailModalOpen: true,
			selectedTodoIdForDetail: todoId,
		});
	},

	closeTodoDetailModal: () => {
		set({
			isTodoDetailModalOpen: false,
			selectedTodoIdForDetail: null,
		});
	},

	openCreateFolderModal: () => {
		set({ isCreateFolderModalOpen: true });
	},

	closeCreateFolderModal: () => {
		set({ isCreateFolderModalOpen: false });
	},

	// 侧边栏操作
	toggleSidebar: () => {
		set((state) => ({ sidebarExpanded: !state.sidebarExpanded }));
	},

	openMobileSidebar: () => {
		set({ mobileSidebarOpen: true });
	},

	closeMobileSidebar: () => {
		set({ mobileSidebarOpen: false });
	},

	selectedFolderId: null,
	todoDetailId: null,

	// 设置选中的文件夹
	setSelectedFolder: (folderId: FolderSelection) => {
		set({ selectedFolderId: folderId });
	},

	// 设置要查看详情的Todo ID
	setTodoDetailId: (todoId: number | null) => {
		set({ todoDetailId: todoId });
	},
}));
