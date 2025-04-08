import { create } from 'zustand';
import { Folder, CreateFolderRequest, UpdateFolderRequest } from '../services/backend/types';
import { folderService } from '../services/backend';

interface FolderState {
  folders: Folder[];
  isLoading: boolean;
  error: string | null;
  
  // 动作
  fetchFolders: () => Promise<void>;
  getFolder: (folderId: number) => Promise<Folder | undefined>;
  addFolder: (folderData: CreateFolderRequest) => Promise<Folder | undefined>;
  updateFolder: (folderId: number, folderData: UpdateFolderRequest) => Promise<Folder | undefined>;
  deleteFolder: (folderId: number) => Promise<void>;
  clearError: () => void;
}

export const useFolderStore = create<FolderState>((set) => ({
  folders: [],
  isLoading: false,
  error: null,
  
  // 获取当前用户的所有文件夹
  fetchFolders: async () => {
    try {
      set({ isLoading: true, error: null });
      const folders = await folderService.getUserFolders();
      set({ folders, isLoading: false });
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '获取文件夹失败',
      });
    }
  },
  
  // 获取单个文件夹
  getFolder: async (folderId: number) => {
    try {
      set({ isLoading: true, error: null });
      const folder = await folderService.getFolder(folderId);
      set({ isLoading: false });
      return folder;
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '获取文件夹详情失败',
      });
      return undefined;
    }
  },
  
  // 添加新文件夹
  addFolder: async (folderData: CreateFolderRequest) => {
    try {
      set({ isLoading: true, error: null });
      const newFolder = await folderService.createFolder(folderData);
      
      // 添加到状态中
      set((state) => ({
        folders: [...state.folders, newFolder],
        isLoading: false,
      }));
      
      return newFolder;
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '创建文件夹失败',
      });
      return undefined;
    }
  },
  
  // 更新文件夹
  updateFolder: async (folderId: number, folderData: UpdateFolderRequest) => {
    try {
      set({ isLoading: true, error: null });
      const updatedFolder = await folderService.updateFolder(folderId, folderData);
      
      // 更新状态中的文件夹
      set((state) => ({
        folders: state.folders.map((folder) => 
          folder.id === folderId ? updatedFolder : folder
        ),
        isLoading: false,
      }));
      
      return updatedFolder;
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '更新文件夹失败',
      });
      return undefined;
    }
  },
  
  // 删除文件夹
  deleteFolder: async (folderId: number) => {
    try {
      set({ isLoading: true, error: null });
      await folderService.deleteFolder(folderId);
      
      // 从状态中删除
      set((state) => ({
        folders: state.folders.filter((folder) => folder.id !== folderId),
        isLoading: false,
      }));
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '删除文件夹失败',
      });
    }
  },
  
  // 清除错误
  clearError: () => {
    set({ error: null });
  },
})); 