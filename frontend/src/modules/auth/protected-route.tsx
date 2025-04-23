import { Navigate, Outlet, useLocation } from "react-router";
import { UserRole, Permission } from "../../services/backend/types";
import { useRoleStore } from "./role.store";

/**
 * PermissionProtectedRouteProps based on permission route protection component interface
 */
export interface PermissionProtectedRouteProps {
  requiredRoles?: UserRole[];
  requiredPermissions?: Permission[];
  redirectPath?: string;
}

/**
 * Check if the user has the required roles and permissions
 */
export function checkPermissionAccess(props: PermissionProtectedRouteProps) {
  const { requiredRoles, requiredPermissions } = props;
  const { currentRole, hasPermission } = useRoleStore.getState();
  
  // 检查角色
  const hasRequiredRole = requiredRoles
    ? requiredRoles.some((role) => currentRole === role)
    : true;
  
  // 检查权限
  const hasRequiredPermission = requiredPermissions
    ? requiredPermissions.every((permission) => hasPermission(permission))
    : true;
  
  return hasRequiredRole && hasRequiredPermission;
}

/**
 * Permission-based route protection component
 * Check if the user has the required roles or permissions, and if not, redirect to the specified path
 */
export function ProtectedRoute({
  requiredRoles,
  requiredPermissions,
  redirectPath = "/login"
}: PermissionProtectedRouteProps) {
  const location = useLocation();
  const hasAccess = checkPermissionAccess({
    requiredRoles,
    requiredPermissions,
    redirectPath
  });
  
  // If the user does not have the required permissions, redirect to the specified path
  if (!hasAccess) {
    return <Navigate to={redirectPath} state={{ from: location }} replace />;
  }
  
  // If the user has the required permissions, render the child routes
  return <Outlet />;
} 