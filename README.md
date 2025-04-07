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

* The backend project uses Flyway. SQL migration scripts are in `src/main/resources/db/migration`.
* Flyway automatically applies pending migrations when the backend container starts.

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
     * Run Flyway migrations automatically when the backend starts.
     
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

**Phase 5: Frontend - Setup & Basic UI**
25. Initialize React project (Vite/CRA) with TypeScript.
26. Install dependencies: `tailwindcss`, `axios`/`Workspace`, `react-router-dom`, `zustand`.
27. Set up Tailwind CSS & Shadcn/ui.
28. Configure basic routing (`react-router-dom`).
29. Create Layout components (Navbar, etc.) and placeholder Page components.
30. Build Login & Registration UI forms.

**Phase 6: Frontend - Authentication & State**
31. Create API service layer for backend communication.
32. Set up **Zustand** store(s) for managing authentication state, user info, and potentially JWT storage/retrieval logic.
33. Implement Login flow (call API, update Zustand store, handle JWT, redirect).
34. Implement Registration flow (call API).
35. Implement Logout flow (clear Zustand store/JWT, redirect).
36. Implement Protected Routes based on Zustand state.

**Phase 7: Frontend - Core Features (Todo/Folder)**
37. Implement Folder API service calls & UI components (List, Create, Update, Delete). Integrate with Zustand state.
38. Implement Todo API service calls & UI components (List, Create, Update, Delete). Integrate with Zustand state.
39. Connect UI to Zustand state and API calls. Handle loading/error states possibly via Zustand.

**Phase 8: Frontend - Advanced Features (Admin/Moderator)**
40. Implement Role-Based Rendering using user role from Zustand store.
41. Create Admin UI: User list, Promote button functionality (calling API, updating state).
42. Create Moderator UI enhancements: View all Folders/Todos, Disable/Enable Todo functionality (calling API, updating state).

**Phase 9: Azure Clarity Integration**
43. Set up Azure Clarity project & get tracking code.
44. Integrate tracking code into the React application.
45. Verify data flow in the Clarity dashboard.

**Phase 10: Documentation Site**
46. Initialize Astro project in `docs` folder & add Starlight.
47. Configure Starlight (sidebar, title).
48. Write documentation content (.md/.mdx files).
49. **Create `Dockerfile` for Docs site** (Build static files, use Nginx or `serve` to host them).
50. Test docs site build/dev server locally.

**Phase 11: Finalization & Dockerization**
51. Optimize backend `Dockerfile` (multi-stage builds).
52. Create optimized frontend `Dockerfile` (multi-stage build with Nginx or serve).
53. **Update `docker-compose.yml` to include the `docs` service**, using its Dockerfile and mapping appropriate ports (e.g., map host port 8081 to the container's serving port).
54. Finalize `docker-compose.yml` (env vars via `.env`, service dependencies `depends_on` if needed).
55. Perform comprehensive End-to-End testing of all three services via `docker-compose up`.
56. Code review, cleanup, add comments.
57. Final `README.md` updates (verify ports, instructions).