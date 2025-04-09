# Auth Module

This module contains all functionality related to user authentication, roles, and permissions.

## Structure

- **types.ts** - Contains shared type definitions such as UserRole and Permission
- **auth.store.ts** - Handles user authentication, login, and registration
- **role.store.ts** - Manages user roles and access control
- **permission.store.ts** - Manages permissions based on user roles
- **index.ts** - Module entry point, exports all functionality

## Design Notes

To avoid circular dependency issues, we use the following strategies:

1. Place shared types in `types.ts`
2. Use a registration function pattern from role.store to auth.store
3. Keep each store focused on its concerns while allowing collaboration

## Usage

```tsx
import { useAuth, useRole, usePermission } from '../../store';

function MyComponent() {
  const { isAuthenticated, user } = useAuth();
  const { isAdmin } = useRole();
  const { hasPermission } = usePermission();
  
  if (hasPermission('todos.own.create')) {
    // User can create tasks
  }
  
  return <div>{isAuthenticated ? 'Logged in' : 'Not logged in'}</div>;
}
``` 