-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index on username for faster lookups during authentication
CREATE INDEX idx_users_username ON users(username);

-- Insert a super admin user with password 'admin123' (BCrypt hashed in real scenario)
-- This is a placeholder; it will be replaced with a properly hashed password in the final version
INSERT INTO users (username, email, password, role)
VALUES ('admin', 'admin@example.com', '{noop}admin123', 'SUPER_ADMIN');

-- Comment for documentation
COMMENT ON TABLE users IS 'Stores user account information';
COMMENT ON COLUMN users.id IS 'Primary key';
COMMENT ON COLUMN users.username IS 'Unique username for login';
COMMENT ON COLUMN users.email IS 'User''s email address';
COMMENT ON COLUMN users.password IS 'Hashed password';
COMMENT ON COLUMN users.role IS 'User role for authorization';
COMMENT ON COLUMN users.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN users.updated_at IS 'Timestamp when the record was last updated'; 