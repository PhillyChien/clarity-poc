import { create } from "zustand";
import { UserRole, Permission } from "./types";
import { useRoleStore } from "./role.store";

// Role permissions mapping
export const ROLE_PERMISSIONS: Record<UserRole, Permission[]> = {
  "NORMAL": [
    // Can only operate on their own tasks and folders
    "todos.own.view",
    "todos.own.create",
    "todos.own.edit", 
    "todos.own.delete",
    "folders.own.view",
    "folders.own.create",
    "folders.own.edit",
    "folders.own.delete",
  ],
  "MODERATOR": [
    // Permissions for their own tasks and folders
    "todos.own.view",
    "todos.own.create",
    "todos.own.edit", 
    "todos.own.delete",
    "folders.own.view",
    "folders.own.create",
    "folders.own.edit",
    "folders.own.delete",
    
    // Special permissions for moderators on others' tasks
    "todos.others.view",
    "todos.others.ban",
    "folders.others.view",
    "users.view"
  ],
  "SUPER_ADMIN": [
    // Admin has all permissions
    // Own tasks and folders
    "todos.own.view",
    "todos.own.create",
    "todos.own.edit", 
    "todos.own.delete",
    "folders.own.view",
    "folders.own.create",
    "folders.own.edit",
    "folders.own.delete",
    
    // Others' tasks and folders
    "todos.others.view",
    "todos.others.ban",
    "folders.others.view",
    
    // User management
    "users.view",
    "users.manage"
  ]
};

interface PermissionState {
  // Check if current user has a specific permission
  hasPermission: (permission: Permission) => boolean;
  // Check if user has any of the specified permissions
  hasAnyPermission: (permissions: Permission[]) => boolean;
  // Check if user has all of the specified permissions
  hasAllPermissions: (permissions: Permission[]) => boolean;
}

export const usePermissionStore = create<PermissionState>()((set, get) => ({
  hasPermission: (permission: Permission): boolean => {
    // Get current user role from role store
    const roleState = useRoleStore.getState();
    const currentRole = roleState.currentRole;
    
    if (!currentRole) {
      // If no user role is set, check through role store functions
      if (roleState.isAdmin()) {
        const permissions = ROLE_PERMISSIONS["SUPER_ADMIN"];
        return permissions.includes(permission);
      } else if (roleState.isModerator()) {
        const permissions = ROLE_PERMISSIONS["MODERATOR"];
        return permissions.includes(permission);
      } else if (roleState.isUser()) {
        const permissions = ROLE_PERMISSIONS["NORMAL"];
        return permissions.includes(permission);
      }
      return false;
    }
    
    const permissions = ROLE_PERMISSIONS[currentRole];
    return permissions.includes(permission);
  },
  
  hasAnyPermission: (permissions: Permission[]): boolean => {
    return permissions.some(permission => get().hasPermission(permission));
  },
  
  hasAllPermissions: (permissions: Permission[]): boolean => {
    return permissions.every(permission => get().hasPermission(permission));
  }
}));

// Provide a Hook for use in components
export function usePermission() {
  const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermissionStore();
  
  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions
  };
} 