-- V3__add_roles_permissions.sql
-- Description: Adds roles, permissions, and links them to users.

-- 1. Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Create permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 3. Create role_permissions join table
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- 4. Add role_id column to users table (initially nullable)
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_id BIGINT;

-- 5. Insert base roles (if they don't exist)
-- Let BIGSERIAL handle the ID generation. Use ON CONFLICT for idempotency.
INSERT INTO roles (name) VALUES
('NORMAL'),
('MODERATOR'),
('SUPER_ADMIN')
ON CONFLICT (name) DO NOTHING;

-- 6. Insert base permissions (if they don't exist)
-- Let BIGSERIAL handle the ID generation. Use ON CONFLICT for idempotency.
INSERT INTO permissions (name) VALUES
('todos.own.view'),
('todos.own.create'),
('todos.own.edit'),
('todos.own.delete'),
('folders.own.view'),
('folders.own.create'),
('folders.own.edit'),
('folders.own.delete'),
('todos.others.view'),
('todos.others.ban'),
('folders.others.view'),
('users.view'),
('users.manage')
ON CONFLICT (name) DO NOTHING;

-- 7. Map Permissions to Roles using cleaner INSERT ... SELECT statements
-- Use ON CONFLICT for idempotency.

-- NORMAL Role Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'NORMAL'
  AND p.name IN (
      'todos.own.view', 'todos.own.create', 'todos.own.edit', 'todos.own.delete',
      'folders.own.view', 'folders.own.create', 'folders.own.edit', 'folders.own.delete'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- MODERATOR Role Permissions (includes NORMAL permissions + extras)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'MODERATOR'
  AND p.name IN (
      -- Inherits NORMAL permissions (listed again for clarity, ON CONFLICT handles duplicates)
      'todos.own.view', 'todos.own.create', 'todos.own.edit', 'todos.own.delete',
      'folders.own.view', 'folders.own.create', 'folders.own.edit', 'folders.own.delete',
      -- Moderator specific permissions
      'todos.others.view', 'todos.others.ban', 'folders.others.view', 'users.view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- SUPER_ADMIN Role Permissions (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p -- Assign all existing permissions
WHERE r.name = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- 8. Update existing users' role_id based on the old 'role' column (assumed to be VARCHAR)
-- Step 1: Try to map based on the old 'role' string column, if it exists.
-- Note: This assumes the old column is named 'role'. Adjust if necessary.
DO $$
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='role') THEN
      UPDATE users u
      SET role_id = r.id
      FROM roles r
      WHERE u.role::VARCHAR = r.name -- Match user's old role name to the new roles table name
        AND u.role_id IS NULL;      -- Only update users not yet assigned a role_id
   END IF;
END $$;

-- Step 2: Assign a default role ('NORMAL') to any user still missing a role_id.
-- This covers users whose old 'role' didn't match, or if the old 'role' column didn't exist.
UPDATE users
SET role_id = (SELECT id FROM roles WHERE name = 'NORMAL')
WHERE role_id IS NULL;


-- 9. Add Foreign Key Constraint to users.role_id
-- This is done *after* populating the column to avoid constraint violations.
ALTER TABLE users ADD CONSTRAINT fk_users_role_id
FOREIGN KEY (role_id) REFERENCES roles(id);


-- 10. Make role_id NOT NULL
-- This is done *after* ensuring all rows have a non-null role_id (Step 8 ensures this).
ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;


-- 11. Drop the old 'role' column (if it exists and is no longer needed)
ALTER TABLE users DROP COLUMN IF EXISTS role;

-- End of V3 migration script