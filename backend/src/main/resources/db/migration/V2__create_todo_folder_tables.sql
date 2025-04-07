-- Create folders table
CREATE TABLE folders (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_folder_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create todos table
CREATE TABLE todos (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    disabled BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT NOT NULL,
    folder_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_todo_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_todo_folder FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_folders_user_id ON folders(user_id);
CREATE INDEX idx_todos_user_id ON todos(user_id);
CREATE INDEX idx_todos_folder_id ON todos(folder_id);

-- Add comments for documentation
COMMENT ON TABLE folders IS 'Stores folder information for organizing todos';
COMMENT ON COLUMN folders.id IS 'Primary key';
COMMENT ON COLUMN folders.name IS 'Folder name';
COMMENT ON COLUMN folders.description IS 'Optional folder description';
COMMENT ON COLUMN folders.user_id IS 'Reference to the user who owns this folder';
COMMENT ON COLUMN folders.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN folders.updated_at IS 'Timestamp when the record was last updated';

COMMENT ON TABLE todos IS 'Stores todo items';
COMMENT ON COLUMN todos.id IS 'Primary key';
COMMENT ON COLUMN todos.title IS 'Todo title';
COMMENT ON COLUMN todos.description IS 'Optional todo description';
COMMENT ON COLUMN todos.completed IS 'Flag indicating if the todo is completed';
COMMENT ON COLUMN todos.disabled IS 'Flag indicating if the todo is disabled (by moderator)';
COMMENT ON COLUMN todos.user_id IS 'Reference to the user who owns this todo';
COMMENT ON COLUMN todos.folder_id IS 'Optional reference to the folder containing this todo';
COMMENT ON COLUMN todos.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN todos.updated_at IS 'Timestamp when the record was last updated'; 