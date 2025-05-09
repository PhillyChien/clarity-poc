// Role types for type safety
export type UserRole = "NORMAL" | "MODERATOR" | "SUPER_ADMIN";

// Define all permissions
export type Permission =
	// Task-related permissions - for own tasks
	| "todos.own.view"
	| "todos.own.create"
	| "todos.own.edit"
	| "todos.own.delete"

	// Task-related permissions - for others' tasks
	| "todos.others.view"
	| "todos.others.ban"

	// Folder-related permissions - for own folders
	| "folders.own.view"
	| "folders.own.create"
	| "folders.own.edit"
	| "folders.own.delete"

	// Folder-related permissions - for others' folders
	| "folders.others.view"

	// User-related permissions
	| "users.view"
	| "users.manage";

// 认证相关类型
export interface LoginRequest {
	username: string;
	password: string;
}

export interface RegisterRequest {
	username: string;
	email: string;
	password: string;
}

// 用户信息响应
export interface MeResponse {
	type: string; // 认证类型，如 "Bearer"
	id: number;
	username: string;
	email: string;
	role: UserRole; // 强类型的角色
	permissions: Permission[]; // 强类型的权限列表
}

// 用户对象
export interface User extends Omit<MeResponse, "type"> {
	id: number;
	username: string;
	email: string;
	role: UserRole; // 强类型的角色
	permissions: Permission[]; // 强类型的权限列表
}

// 文件夹相关类型
export interface Folder {
	id: number;
	name: string;
	description: string;
	ownerId: number;
	ownerUsername: string;
	todoCount: number;
	createdAt: string;
	updatedAt: string;
}

export interface CreateFolderRequest {
	name: string;
	description?: string;
}

export interface UpdateFolderRequest {
	name: string;
	description?: string;
}

// Todo相关类型
export interface Todo {
	id: number;
	title: string;
	description?: string;
	completed: boolean;
	disabled: boolean;
	ownerId: number;
	ownerUsername: string;
	folderId?: number;
	folderName?: string;
	createdAt: string;
	updatedAt: string;
}

export interface CreateTodoRequest {
	title: string;
	description?: string;
	completed?: boolean;
	folderId?: number;
}

export interface UpdateTodoRequest {
	title?: string;
	description?: string;
	completed?: boolean;
	folderId?: number;
}

// 管理员相关类型
export interface UserResponse {
	id: number;
	username: string;
	email: string;
	role: UserRole; // 强类型的角色
	createdAt: string;
	updatedAt: string;
}

// 角色更新请求
export interface RoleUpdateRequest {
	userId: number;
	role: UserRole; // 强类型的角色
}

// 消息响应类型
export interface MessageResponse {
	message: string;
}

// 错误响应类型
export interface ErrorResponse {
	message: string;
	status: number;
	timestamp: string;
}

// 通用API响应类型
export interface ApiResponse<T> {
	data?: T;
	message?: string;
	success: boolean;
}
