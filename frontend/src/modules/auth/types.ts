// Role types for type safety
export type UserRole = "NORMAL" | "MODERATOR" | "SUPER_ADMIN";

// Permission types
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
