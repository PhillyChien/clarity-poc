# Clarity POC

## Introduction

This repository contains a multi-part project designed primarily to evaluate the effectiveness and suitability of **Microsoft Azure Clarity** for monitoring user behavior. It includes a full-stack web application (frontend and backend) and a dedicated documentation site, all manageable via Docker Compose.

The core goal is to implement basic web functionalities (user management, Todo/Folder organization) within the application, integrate Azure Clarity on the frontend, and then analyze the collected data (analytics, heatmaps, session recordings) via the Clarity dashboard to determine its value for this specific scenario.

## Repository Structure

This repository is organized into three main containerized parts, managed by Docker Compose:

1.  **Frontend:** A React-based single-page application providing the user interface, using Zustand for state management, and integrating Azure Clarity.
2.  **Backend:** A Spring MVC application providing RESTful APIs. Database schema changes are managed using Flyway.
3.  **Docs:** A static documentation site built using Astro and the Starlight template, providing detailed project information.

## Technology Stack

This project utilizes the following technologies across its components:

**Backend (API Service):**

* **Framework:** Spring MVC (Java)
* **Security:** Spring Security
* **Authentication:** Basic username/password login/registration.
* **Authorization:** JWT (JSON Web Tokens)
* **Database:** PostgreSQL
* **Database Migrations:** Flyway
* **Dependencies:** Spring Data JPA, Lombok, Validation

**Frontend (Web Application):**

* **Library/Framework:** React
* **Language:** TypeScript
* **Styling:** Tailwind CSS
* **UI Components:** Shadcn/ui
* **State Management:** Zustand
* **HTTP Client:** Axios or Fetch API
* **Routing:** React Router DOM
* **Monitoring:** Microsoft Azure Clarity (integrated script)

**Docs (Documentation Site):**

* **Framework:** Astro
* **Language:** TypeScript
* **Template/UI:** Starlight

**Infrastructure:**

* **Containerization:** Docker (for Backend, Frontend, and Docs)
* **Orchestration:** Docker Compose (Manages all services: Backend, Frontend, Docs, Database)

## How it works

### Architecture

The application follows a decoupled architecture orchestrated by Docker Compose:
1.  A **React frontend** container serves the UI. State is managed globally using Zustand. Azure Clarity is integrated here.
2.  A **Spring MVC backend** container provides RESTful APIs. Database migrations are handled by Flyway upon startup.
3.  An **Astro docs** container serves the static documentation site.
4.  A **PostgreSQL** container provides the database service.

### Authentication and Authorization (Web Application)

* Users can register for an account or log in using their credentials via the frontend.
* Upon successful login, the backend issues a JWT containing the user's role. This token is managed by the frontend (likely stored in localStorage/sessionStorage and handled via Zustand state/actions) and sent with subsequent requests.
* **Roles:**
    * `Normal`: Default role. Manages own Todos/Folders.
    * `Moderator`: Promoted by Admin. Manages own items, views all items, can disable Todos.
    * `Super Admin`: Pre-configured. Manages users (view, promote).

### Core Features (Web Application)

* **User Management:** Registration, Login. Admin can view users and promote `Normal` users.
* **Todo & Folder Management:** `Normal`/`Moderator` manage their own. `Moderator` can view all and disable Todos.

### Database Migrations (Flyway - Backend)

* The backend project uses Flyway for database schema migrations. SQL migration scripts are located in `src/main/resources/db/migration`.
* Migration scripts follow a versioned naming convention: `V{version}__{description}.sql` (e.g., `V1__init_user_tables.sql`).

#### Development Environment Migration Approach

* In development, Flyway migrations are configured to run automatically when the application starts.
* This automatic approach simplifies the development workflow and ensures database schema is always in sync with the application code.
* Alternatively, you can manually execute migrations using the Maven Flyway plugin:
  ```bash
  # Navigate to the backend directory
  cd backend
  
  # 執行 Flyway 遷移（環境無關）
  ./mvnw flyway:migrate
  
  # 明確指定在開發環境執行遷移（推薦方式）
  ./mvnw flyway:migrate -Dspring.profiles.active=dev
  
  # 明確指定在生產環境執行遷移
  ./mvnw flyway:migrate -Dspring.profiles.active=prod
  ```

#### Enterprise-grade Migration Practices

For production and enterprise environments, we recommend a more controlled migration approach:

1. **Migration Environments**:
   * **Development**: Automatic migrations on application startup (current implementation)
   * **Testing/Staging**: CI/CD pipeline-controlled migrations, separate from application deployment
   * **Production**: Manual or controlled migrations with proper approvals

2. **Production Migration Process**:
   * Migrations should be executed as a separate step before application deployment
   * Use dedicated migration commands rather than relying on application startup:
     ```bash
     # Using Maven with properties from application.yml/application-prod.yml
     mvn flyway:migrate -Dspring.profiles.active=prod
     
     # Using Flyway CLI directly
     flyway -url=jdbc:postgresql://db-host:5432/clarity_db -user=postgres -password=securepassword migrate
     ```

3. **Enterprise Best Practices**:
   * Database changes should be reviewed by DBAs or senior developers
   * Always create database backups before running migrations
   * Consider blue-green deployment strategies for zero-downtime migrations
   * Maintain separate migration scripts for data and schema changes
   * Test migrations thoroughly in staging environments that mirror production
   * For high-regulation industries (finance, healthcare), implement additional approval workflows

These practices ensure database changes are applied in a controlled, predictable manner with proper oversight, especially important for business-critical applications.

### Azure Clarity Integration (Frontend)

* The Azure Clarity tracking script is embedded within the frontend application container.
* It actively monitors user interactions for analysis in the Azure Clarity dashboard.

## Running the Project

### Development Environment 

For local development, we provide a dedicated setup to make the development process easier:

1. **Prerequisites:**
   * Docker and Docker Compose installed (for PostgreSQL database)
   * Java 17 (for running the backend directly)
   * Node.js (for frontend development, when implemented)

2. **Starting the Development Environment:**
   * Use the provided script to start the development environment:
     ```bash
     ./start-dev.sh
     ```
   * This script will:
     * Start a PostgreSQL database in a Docker container
     * Start pgAdmin for database management
     * Launch the backend application with the development profile
     
3. **Development Services:**
   * **PostgreSQL:** `localhost:5432` (credentials: postgres/postgres)
   * **pgAdmin:** `http://localhost:5050` (login with admin@example.com / admin123)
   * **Backend API:** `http://localhost:8080/api`

4. **Manual Development Setup:**
   If you prefer to start services manually:
   * Start the database:
     ```bash
     docker-compose -f docker-compose.dev.yml up -d
     ```
   * Start the backend (from the backend directory):
     ```bash
     cd backend
     ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
     ```

5. **Running Tests:**
   * Tests use H2 in-memory database and can be run without Docker:
     ```bash
     cd backend
     ./mvnw test
     ```

### Production Environment

The entire project stack (Frontend, Backend, Docs, Database) is designed to run locally using a single Docker Compose command.

1. **Prerequisites:**
   * Docker installed
   * Docker Compose installed
   
2. **Environment Configuration:**
   * Ensure you have `Dockerfile`s present in the `backend`, `frontend`, and `docs` directories.
   * Copy any `.env.example` files to `.env` in the project root and configure necessary variables (e.g., Database credentials, JWT secret).
   
3. **Build and Run:**
   * Navigate to the project's root directory (where the `docker-compose.yml` file is located).
   * Run the command:
     ```bash
     docker-compose up --build -d
     ```
   * This command will:
     * Build the images for the frontend, backend, docs, and database (if not already built).
     * Start all services in detached mode.
     * Run Flyway migrations automatically when the backend starts (for development purposes only; see Enterprise-grade Migration Practices section for production recommendations).
     
4. **Accessing the Services:**
   * **Frontend:** `http://localhost:3000` (or the port configured in `docker-compose.yml`)
   * **Backend API:** `http://localhost:8080/api` (or the port configured)
   * **Docs Site:** `http://localhost:8081` (or the port configured - *Note: This example assumes the docs container serves on port 80/3000 internally and is mapped to host 8081*)

*(Adjust port numbers based on your actual `docker-compose.yml` configuration.)*

## Project Roadmap

This roadmap outlines the suggested phases and steps to build the project sequentially.

**Phase 0: Project Setup & Foundation**
1.  Initialize Git repository & basic folder structure (`backend`, `frontend`, `docs`).
2.  Set up initial `docker-compose.yml` with PostgreSQL service.
3.  Create initial `README.md`.

**Phase 1: Backend - Core & Authentication**
4.  Initialize Spring Boot project (Web, Security, JPA, PostgreSQL, Lombok, Validation).
5.  Configure DB connection. Add **Flyway** dependency and configure it. Create initial migration script (V1__...).
6.  Define `User` entity and `Role` enum/entity.
7.  Basic Spring Security config (`PasswordEncoder`, `SecurityFilterChain` placeholders).
8.  Implement User Registration (Controller, Service, Repository, DTO, Password Hashing).
9.  Implement User Login & JWT generation/validation (JWT dependencies, Filters, Controller endpoint).
10. Seed `Super Admin` user via `CommandLineRunner` or Flyway script.
11. Basic exception handling.
12. Create backend `Dockerfile`. Update `docker-compose.yml` (add `backend` service).

**Phase 2: Backend - Core Features (Todo/Folder)**
13. Define `Folder` and `Todo` entities with relationships. Add Flyway scripts for new tables.
14. Implement Folder CRUD APIs (Controller, Service, Repository) with ownership authorization checks.
15. Implement Todo CRUD APIs (Controller, Service, Repository) with ownership authorization checks.
16. Test backend API endpoints.

**Phase 3: Backend - Enable Swagger**
17. Add Swagger dependencies.
18. Configure Swagger UI.

**Phase 4: Backend - Admin/Moderator Features**
18. Implement Admin endpoint: List Users.
19. Implement Admin endpoint: Promote User Role.
20. Implement Moderator endpoint: View All Folders.
21. Implement Moderator endpoint: View All Todos.
22. Implement Moderator endpoint: Disable/Enable Todo.
23. Refine endpoint authorization (`@PreAuthorize`).
24. Test admin/moderator API endpoints.

**Phase 5: Frontend - Setup**
25. Initialize React project (Vite/CRA) with TypeScript called `frontend`.
26. Install dependencies: `tailwindcss`, `react-router`, `zustand`.
27. Set up Tailwind CSS & Shadcn/ui.
28. Configure basic routing (`react-router`).
29. Build Login & Registration UI forms.
30. Setup biome config under `frontend` folder.
31. Complete frontend README.md

**Phase 6: Frontend - Service Layer & State Management**
32. Create API service layer for backend communication.
33. Set up **Zustand** store(s) for managing state.
34. Implement Zustand store(s).

**Phase 7: Frontend - Login/Registration**
35. Build Login (LoginPage, LoginForm) & Registration (RegisterPage, RegisterForm) UI components using Shadcn.
36. Implement authService functions (loginUser, registerUser) using apiClient.
37. Implement Login flow: Connect UI form -> call authService.loginUser -> update authStore (token, user) -> handle JWT storage -> redirect to /todos. Handle loading/error states.
38. Implement Registration flow: Connect UI form -> call authService.registerUser -> handle success (e.g., redirect to login) / error states.
39. Implement ProtectedRoute component (basic check for isAuthenticated from authStore). Apply to /todos.

**Phase 8: Frontend - Todo**
40. Implement MainLayout component (Header, Sidebar placeholder, Content Area). Include Header logic for displaying user info and Logout button.
41. Implement Logout functionality (clear authStore, clear token storage, redirect to /login).
42. Implement folderService (fetch, create, update, delete). Implement folderStore actions calling the service and updating state.
43. Implement FolderTree/FolderList component: Fetch/display folders from folderStore, handle selection (update uiStore), implement Create/Rename/Delete actions (trigger modals, call folderService, update folderStore). Handle folder loading/error/empty states.
44. Implement todoService (fetchByFolder, create, update, delete, toggleComplete). Implement todoStore actions calling the service and updating state.
45. Implement TodoList component: Fetch/display todos based on selected folder (from uiStore & todoStore), handle loading/error/empty states.
46. Implement TodoItem component: Display todo data, handle completion toggle (call todoService, update todoStore), handle delete (confirmation, call todoService, update todoStore), trigger edit (open detail modal).
47. Implement AddTodoForm component: Input + submit logic (call todoService.createTodo with selected folder context, update todoStore).
48. (Optional) Implement TodoDetailModal: Form for comprehensive editing, triggered from TodoItem, calls todoService.updateTodo, updates todoStore.

**Phase 9: Frontend - Moderator**
49. Implement role checking logic (e.g., useAuth hook accessing authStore) for conditional rendering. Update ProtectedRoute to handle role checks.
50. Implement moderatorService functions (fetchAllTodos, disableTodo, enableTodo).
51. Create ModeratorViewPage (/moderator/view-all) with role-protected route (Moderator or Super Admin).
52. Implement AllTodosList component: Fetch/display all todos using moderatorService, show owner info.
53. Implement Disable/Enable buttons within AllTodosList (conditionally rendered): On click -> call moderatorService -> update relevant state (e.g., todoStore or a dedicated moderatorStore) visually.

**Phase 10: Frontend - Admin**
54. Implement adminService functions (fetchAllUsers, promoteUser).
55. Create UserManagementPage (/admin/users) with role-protected route (Super Admin only).
56. Implement UserList component: Fetch/display all users using adminService, show roles.
57. Implement "Promote to Moderator" button within UserList (conditionally rendered): On click -> call adminService.promoteUser -> update user list state visually.

**Phase 11: Azure Clarity Integration**
58. Set up Azure Clarity project & get tracking code.
59. Integrate tracking code into the React application.
60. Verify data flow in the Clarity dashboard.

**Phase 12: Documentation Site**
59. Initialize Astro project in `docs` folder & add Starlight.
60. Configure Starlight (sidebar, title).
61. Write documentation content (.md/.mdx files).
62. **Create `Dockerfile` for Docs site** (Build static files, use Nginx or `serve` to host them).
63. Test docs site build/dev server locally.

**Phase 13: Finalization & Dockerization**
61. Optimize backend `Dockerfile` (multi-stage builds).
62. Create optimized frontend `Dockerfile` (multi-stage build with Nginx or serve).
63. **Update `docker-compose.yml` to include the `docs` service**, using its Dockerfile and mapping appropriate ports (e.g., map host port 8081 to the container's serving port).
64. Finalize `docker-compose.yml` (env vars via `.env`, service dependencies `depends_on` if needed).
65. Perform comprehensive End-to-End testing of all three services via `docker-compose up`.
66. Code review, cleanup, add comments.
67. Final `README.md` updates (verify ports, instructions).