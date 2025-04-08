import { apiClient } from "./apiClient";
import type { CreateFolderRequest, Folder, UpdateFolderRequest } from "./types";

/**
 * 文件夹服务 - 负责与后端API交互进行文件夹相关操作
 */
export const folderService = {
	/**
	 * 获取当前用户的所有文件夹
	 * @returns 文件夹数组
	 */
	getUserFolders: (): Promise<Folder[]> => {
		return apiClient.get<Folder[]>("/folders");
	},

	/**
	 * 获取单个文件夹详情
	 * @param folderId 文件夹ID
	 * @returns 文件夹对象
	 */
	getFolder: (folderId: number): Promise<Folder> => {
		return apiClient.get<Folder>(`/folders/${folderId}`);
	},

	/**
	 * 创建新文件夹
	 * @param folderData 文件夹数据
	 * @returns 创建的文件夹对象
	 */
	createFolder: (folderData: CreateFolderRequest): Promise<Folder> => {
		return apiClient.post<Folder>("/folders", folderData);
	},

	/**
	 * 更新文件夹
	 * @param folderId 文件夹ID
	 * @param folderData 更新的文件夹数据
	 * @returns 更新后的文件夹对象
	 */
	updateFolder: (
		folderId: number,
		folderData: UpdateFolderRequest,
	): Promise<Folder> => {
		return apiClient.put<Folder>(`/folders/${folderId}`, folderData);
	},

	/**
	 * 删除文件夹
	 * @param folderId 文件夹ID
	 * @returns void
	 */
	deleteFolder: (folderId: number): Promise<void> => {
		return apiClient.delete<void>(`/folders/${folderId}`);
	},
};
