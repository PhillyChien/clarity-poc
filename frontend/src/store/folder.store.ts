import { create } from "zustand";
import { folderService } from "../services/backend/folderService";
import { usersService } from "../services/backend";
import type {
	CreateFolderRequest,
	Folder,
	UpdateFolderRequest,
} from "../services/backend/types";
import { useTodoTreeStore } from "./todo-tree.store";

interface FolderState {
	folders: Folder[];
	isLoading: boolean;
	error: string | null;

	// 动作
	fetchUserFolders: () => Promise<void>;
	fetchFoldersByUserId: (userId: number) => Promise<void>;
	getFolder: (folderId: number) => Promise<Folder | undefined>;
	addFolder: (folderData: CreateFolderRequest) => Promise<Folder | undefined>;
	updateFolder: (
		folderId: number,
		folderData: UpdateFolderRequest,
	) => Promise<Folder | undefined>;
	deleteFolder: (folderId: number) => Promise<void>;
	clearError: () => void;
	resetStore: () => void;
}

export const useFolderStore = create<FolderState>((set) => ({
	folders: [],
	isLoading: false,
	error: null,

	// 获取当前用户的所有文件夹
	fetchUserFolders: async () => {
		try {
			set({ isLoading: true, error: null });
			const folders = await folderService.getUserFolders();
			set({ folders, isLoading: false });
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "获取文件夹失败",
			});
		}
	},

	// 获取指定用户的所有文件夹 (版主功能)
	fetchFoldersByUserId: async (userId: number) => {
		try {
			set({ isLoading: true, error: null });
			const folders = await usersService.getUserFolders(userId);
			set({ folders, isLoading: false });
		} catch (error: unknown) {
			console.error("Error fetching user folders:", error);
			set({
				folders: [],
				isLoading: false,
				error: error instanceof Error ? error.message : "获取用户文件夹失败",
			});
		}
	},

	// 获取单个文件夹详情
	getFolder: async (folderId: number) => {
		try {
			set({ isLoading: true, error: null });
			const folder = await folderService.getFolder(folderId);
			set({ isLoading: false });
			return folder;
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "获取文件夹详情失败",
			});
			return undefined;
		}
	},

	// 添加新文件夹
	addFolder: async (folderData: CreateFolderRequest) => {
		try {
			set({ isLoading: true, error: null });
			const newFolder = await folderService.createFolder(folderData);
			set((state) => ({
				folders: [...state.folders, newFolder],
				isLoading: false,
			}));
			return newFolder;
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "创建文件夹失败",
			});
			return undefined;
		}
	},

	// 更新文件夹
	updateFolder: async (folderId: number, folderData: UpdateFolderRequest) => {
		try {
			set({ isLoading: true, error: null });
			const updatedFolder = await folderService.updateFolder(
				folderId,
				folderData,
			);
			set((state) => ({
				folders: state.folders.map((folder) =>
					folder.id === folderId ? updatedFolder : folder,
				),
				isLoading: false,
			}));
			return updatedFolder;
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "更新文件夹失败",
			});
			return undefined;
		}
	},

	// 删除文件夹
	deleteFolder: async (folderId: number) => {
		try {
			set({ isLoading: true, error: null });
			await folderService.deleteFolder(folderId);
			set((state) => ({
				folders: state.folders.filter((folder) => folder.id !== folderId),
				isLoading: false,
			}));

			// 检查当前选中的项目是否为刚删除的文件夹，如果是则清除选择状态
			const todoTreeStore = useTodoTreeStore.getState();
			if (
				todoTreeStore.selectedItemId === folderId &&
				todoTreeStore.selectedItemType === "folder"
			) {
				console.log(`清除已删除的文件夹 ${folderId} 的选择状态`);
				useTodoTreeStore.setState({
					selectedItemId: null,
					selectedItemType: null,
				});
			}
		} catch (error: unknown) {
			set({
				isLoading: false,
				error: error instanceof Error ? error.message : "删除文件夹失败",
			});
		}
	},

	// 清除错误
	clearError: () => {
		set({ error: null });
	},

	// 重置存储
	resetStore: () => {
		set({ folders: [], isLoading: false, error: null });
	},
}));
