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
* **HTTP Client:** Fetch API
* **Routing:** React Router DOM
* **Monitoring:** Microsoft Azure Clarity (integrated script)

**Docs (Documentation Site):**

* **Framework:** Astro
* **Language:** TypeScript
* **Template/UI:** Starlight

**Infrastructure:**

* **Local Development:**
  * **Containerization:** Docker (for Backend, Frontend, and Docs)
  * **Orchestration:** Docker Compose (Manages all services: Backend, Frontend, Docs, Database)
  
* **Cloud Deployment (Azure):**
  * **IaC Tool:** Terraform
  * **Container Registry:** Azure Container Registry (ACR)
  * **Compute:** Azure App Services (Frontend + Backend)
  * **Database:** Azure PostgreSQL
  * **Security:** Azure Key Vault, Virtual Network
  * **Monitoring:** Application Insights, Azure Clarity

## How it works

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
* In development, Flyway migrations are configured to run automatically when the application starts.
* You can manually execute migrations using the Maven Flyway plugin:
  ```bash
  # Navigate to the backend directory
  cd backend
  
  # Execute Flyway migrations (environment-independent)
  ./mvnw flyway:migrate
  
  # Specify the development environment explicitly (recommended)
  ./mvnw flyway:migrate -Dspring.profiles.active=dev
  ```

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
   * Docker and Docker Compose installed
   
2. **Environment Configuration:**
   * Ensure you have `Dockerfile`s present in the `backend`, `frontend`, and `docs` directories.
   * Copy any `.env.example` files to `.env` in the project root and configure necessary variables (e.g., Database credentials, JWT secret).
   
3. **Build and Run:**
   * Navigate to the project's root directory and run:
     ```bash
     docker-compose up --build -d
     ```
     
4. **Accessing the Services:**
   * **Frontend:** `http://localhost:3000` (or the port configured in `docker-compose.yml`)
   * **Backend API:** `http://localhost:8080/api` (or the port configured)
   * **Docs Site:** `http://localhost:8081` (or the port configured)

*(Adjust port numbers based on your actual `docker-compose.yml` configuration.)*
