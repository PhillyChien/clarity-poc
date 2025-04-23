// Export all auth module content (except functions moved to protected-route)
export * from "./auth.store";
export * from "./role.store";

// Export ProtectedRoute component and related functions from protected-route.tsx
export {
  ProtectedRoute,
  checkPermissionAccess,
  type PermissionProtectedRouteProps
} from "./protected-route";