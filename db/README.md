# Database

## Migrations

This project uses Flyway for database migration management. SQL migration scripts are located in the `db/migration/` directory.

### Migration Script Naming Convention

Migration scripts follow a versioned naming convention: `V{version}__{description}.sql` (e.g., `V1__init_user_tables.sql`).

### Using Flyway CLI Tool

#### Installing Flyway CLI (macOS)

```bash
# Install Flyway CLI using Homebrew
brew install flyway
```

#### Configuration

The Flyway configuration file is located at `db/flyway/flyway.conf`, containing database connection and migration-related settings.

#### Executing Database Migrations

```bash
# Navigate to the project root directory
cd clarity-poc

# Execute migrations (using the config file)
flyway -configFiles=db/flyway/flyway.conf migrate

# Check migration status
flyway -configFiles=db/flyway/flyway.conf info

# Clean the database (use with caution, development environment only)
flyway -configFiles=db/flyway/flyway.conf clean
```

### Current Migration Scripts

- `V1__init_user_tables.sql`: Initializes user tables
- `V2__create_todo_folder_tables.sql`: Creates todo and folder tables
