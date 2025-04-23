import { create } from "zustand";
import { getCurrentUser, registerRoleStoreSetCurrentRole, registerRoleStoreSetCurrentPermissions } from "./auth.store";
import { UserRole, Permission } from "../../services/backend/types";


interface RoleState {
	// Current role
	currentRole: UserRole | null;

	// Current permissions
	currentPermissions: Permission[];

	// Role check methods
	hasRole: (role: UserRole) => boolean;
	isAdmin: () => boolean;
	isModerator: () => boolean;
	isUser: () => boolean;

	// Role hierarchy methods
	getRoleHierarchy: () => { [key in UserRole]: number };
	compareRoles: (role1: UserRole | null, role2: UserRole | null) => number;

	// Check if a role can access another role's functionality
	canAccessRoleLevel: (baseRole: UserRole, targetRole: UserRole) => boolean;

	// Add method to set the current role directly
	setCurrentRole: (role: UserRole | null) => void;
	
	// Set the current user's permission list - use the Permission type to enhance type safety
	setCurrentPermissions: (permissions: Permission[]) => void;

	// Permission methods
	hasPermission: (permission: Permission) => boolean;
	hasAnyPermission: (permissions: Permission[]) => boolean;
	hasAllPermissions: (permissions: Permission[]) => boolean;
}

export const useRoleStore = create<RoleState>()((set, get) => ({
	currentRole: null,
	currentPermissions: [],

	hasRole: (role: UserRole): boolean => {
		const user = getCurrentUser();
		if (!user) return false;

		const userRole = user.role as UserRole;

		// Super admin has access to everything
		if (userRole === "SUPER_ADMIN") return true;

		// Moderator has access to moderator and user roles
		if (userRole === "MODERATOR" && (role === "MODERATOR" || role === "NORMAL"))
			return true;

		// Regular user only has access to user role
		if (userRole === "NORMAL" && role === "NORMAL") return true;

		return false;
	},

	isAdmin: (): boolean => {
		const user = getCurrentUser();
		return user?.role === "SUPER_ADMIN" || false;
	},

	isModerator: (): boolean => {
		const user = getCurrentUser();
		return user?.role === "MODERATOR" || false;
	},

	isUser: (): boolean => {
		return get().hasRole("NORMAL");
	},

	getRoleHierarchy: () => ({
		SUPER_ADMIN: 3,
		MODERATOR: 2,
		NORMAL: 1,
	}),

	compareRoles: (role1: UserRole | null, role2: UserRole | null): number => {
		const hierarchy = get().getRoleHierarchy();
		const role1Value = role1 ? hierarchy[role1] : 0;
		const role2Value = role2 ? hierarchy[role2] : 0;

		return role1Value - role2Value;
	},

	canAccessRoleLevel: (baseRole: UserRole, targetRole: UserRole): boolean => {
		const hierarchy = get().getRoleHierarchy();
		return hierarchy[baseRole] >= hierarchy[targetRole];
	},

	// Method to set the current role directly
	setCurrentRole: (role: UserRole | null) => {
		set({ currentRole: role });
	},
	
	// Method to set the current permissions
	setCurrentPermissions: (permissions: Permission[]) => {
		set({ currentPermissions: permissions });
	},

	// Permission methods - 只使用后端返回的权限列表
	hasPermission: (permission: Permission): boolean => {
		const { currentPermissions } = get();
		
		// Check if the permission is in the list returned by the backend
		return currentPermissions.includes(permission);
	},

	hasAnyPermission: (permissions: Permission[]): boolean => {
		return permissions.some((permission) => get().hasPermission(permission));
	},

	hasAllPermissions: (permissions: Permission[]): boolean => {
		return permissions.every((permission) => get().hasPermission(permission));
	},
}));

// Register the setCurrentRole function with auth store to avoid circular dependency
registerRoleStoreSetCurrentRole(useRoleStore.getState().setCurrentRole);

// Register the setCurrentPermissions function with auth store to avoid circular dependency
registerRoleStoreSetCurrentPermissions(useRoleStore.getState().setCurrentPermissions);

// Provide a Hook for use in components
export function useRole() {
	const {
		currentRole,
		currentPermissions,
		hasRole,
		isAdmin,
		isModerator,
		isUser,
		canAccessRoleLevel,
		compareRoles,
		setCurrentRole,
		setCurrentPermissions,
		hasPermission,
		hasAnyPermission,
		hasAllPermissions,
	} = useRoleStore();

	return {
		currentRole,
		currentPermissions,
		hasRole,
		isAdmin,
		isModerator,
		isUser,
		canAccessRoleLevel,
		compareRoles,
		setCurrentRole,
		setCurrentPermissions,
		// Helper to get current role with type safety
		getCurrentRole: () => currentRole,
		// Permission methods
		hasPermission,
		hasAnyPermission,
		hasAllPermissions,
	};
}

// Utility function to get role name
export function getRoleName(role: UserRole): string {
	switch (role) {
		case "SUPER_ADMIN":
			return "Administrator";
		case "MODERATOR":
			return "Moderator";
		case "NORMAL":
			return "User";
		default:
			return "Unknown";
	}
}
