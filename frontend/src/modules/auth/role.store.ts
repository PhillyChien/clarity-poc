import { create } from "zustand";
import { useEffect } from "react";
import type React from "react";
import { useNavigate } from "react-router";
import { UserRole } from "./types";
import { getCurrentUser, registerRoleStoreSetCurrentRole } from "./auth.store";

interface RoleState {
  // Current role (derived from auth store)
  currentRole: UserRole | null;
  
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
}

export const useRoleStore = create<RoleState>()((set, get) => ({
  currentRole: null,
  
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
    "SUPER_ADMIN": 3,
    "MODERATOR": 2,
    "NORMAL": 1
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
  }
}));

// Register the setCurrentRole function with auth store to avoid circular dependency
registerRoleStoreSetCurrentRole(useRoleStore.getState().setCurrentRole);

// Provide a Hook for use in components
export function useRole() {
  const { 
    currentRole,
    hasRole, 
    isAdmin, 
    isModerator, 
    isUser,
    canAccessRoleLevel,
    compareRoles,
    setCurrentRole
  } = useRoleStore();
  
  return {
    currentRole,
    hasRole,
    isAdmin,
    isModerator,
    isUser,
    canAccessRoleLevel,
    compareRoles,
    setCurrentRole,
    // Helper to get current role with type safety
    getCurrentRole: () => currentRole
  };
}

// Role-based protected route component
export function ProtectedRoute({
	children,
	requiredRole = "NORMAL",
}: {
	children: React.ReactNode;
	requiredRole?: UserRole;
}): React.ReactNode {
	const navigate = useNavigate();
  // We'll now use our auth function directly
  const isAuthenticated = !!getCurrentUser();
  const { hasRole } = useRole();

	useEffect(() => {
		// Redirect to login page if not authenticated
		if (!isAuthenticated) {
			navigate("/login");
			return;
		}

		// Redirect to todos page if authenticated but doesn't have required role
		if (!hasRole(requiredRole)) {
			navigate("/todos");
		}
	}, [isAuthenticated, requiredRole, navigate, hasRole]);

	// Don't render children if not authenticated or doesn't have required role
	if (!isAuthenticated || !hasRole(requiredRole)) {
		return null;
	}

	return children;
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