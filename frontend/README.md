# Frontend - Clarity POC Application

## Introduction

This directory contains the frontend React application for the Clarity POC project. It provides the user interface for interacting with the backend API, managing Todos and Folders, handling user authentication, and includes specific views for different user roles (Normal, Moderator, Super Admin). Azure Clarity is integrated into this frontend to monitor user behavior.

The goal is to create a functional, user-friendly interface that serves as a realistic environment for evaluating Azure Clarity's capabilities.

**Key technologies used:**

* **Framework/Library:** React
* **Language:** TypeScript
* **State Management:** Zustand
* **Routing:** React Router DOM
* **Styling:** Tailwind CSS
* **UI Components:** Shadcn/ui
* **HTTP Client:** Fetch API (Native Browser API)
* **Linting/Formatting:** Biome

## Getting Started (Local Development)

Follow these steps to set up and run the frontend application locally for development purposes.

**Prerequisites:**

1.  **Node.js and Package Manager:** Node.js (version specified in `.nvmrc` or project docs, e.g., LTS) and a package manager (pnpm is often preferred, but npm or yarn can also be used).
2.  **Running Backend & Database:** The backend API service and PostgreSQL database **must be running**. For local development, this is typically achieved using the `start-dev.sh` script or the `docker-compose.dev.yml` file located in the **root** of the project repository, as described in the main project README. Ensure the backend is accessible (usually at `http://localhost:8080`).

**Setup & Running:**

1.  **Navigate to Frontend Directory:**
    Open your terminal and change to the frontend directory:
    ```bash
    cd frontend
    ```

2.  **Install Dependencies:**
    Install the project dependencies using your chosen package manager:
    ```bash
    # Using pnpm
    pnpm install
    ```

3.  **Configure Environment Variables:**
    * Look for an `.env.example` file in the `frontend` directory.
    * Copy it to create your own environment file:
        ```bash
        cp .env.example .env.local
        ```
    * Edit the `.env.local` file and ensure the variables are set correctly, especially the one pointing to your running backend API:
        ```dotenv
        # Example for Vite-based projects
        VITE_API_BASE_URL=http://localhost:8080
        ```

4.  **Run the Development Server:**
    Start the frontend development server:
    ```bash
    pnpm run dev
    ```

5.  **Access the Application:**
    * The terminal output will indicate the local address where the frontend is being served. This is often:
        * `http://localhost:5173` (common for Vite)
    * Open this URL in your web browser. You should see the login or registration page.

**Development Notes:**

* The development server usually supports Hot Module Replacement (HMR) for faster feedback during development.
* Ensure the backend API URL configured in your `.env` file matches where your backend is actually running.
* Refer to the Biome configuration (`biome.json`) for code linting and formatting standards. Run `pnpm check` and `pnpm check:fix` to check your code.

## CI/CD Pipeline

The frontend application is configured to use GitHub Actions for CI/CD. The workflow is defined in `.github/workflows/frontend-ci.yml`.

## Environment Variable Injection

This frontend application requires certain configuration values at runtime (e.g., the backend API URL). Since standard Vite/React `.env` file handling embeds variables at *build time*, we use a **runtime injection** strategy to allow the *same Docker image* to be configured differently across environments (local, staging, production) without rebuilding.

The core idea is to generate a `config.js` file dynamically, which populates a global `window._env_` object. The React application then reads its configuration from this object.

**1. Local Development:**

* Configuration values for local development are stored in the `.env.development` (or `.env.local`) file in the `frontend` directory. Ensure variable names are prefixed with `VITE_` (e.g., `VITE_API_BASE_URL`).
* When you run `pnpm run dev`, the `generate-config.mjs` script executes first.
* This script reads the variables from your `.env.development` file and generates a `public/config.js` file.
* The `public/index.html` file includes `<script src="/config.js"></script>`, which loads the generated file and makes the configuration available under `window._env_` for the running application.
* **Note:** `public/config.js` is generated locally and should be listed in `.gitignore`.

**2. Production/Deployment (Docker on Azure App Service):**

* In deployed environments (like Azure App Service), environment variables are injected into the running container. This is typically configured using App Service Application Settings, often linked to Azure Key Vault secrets using Key Vault References. Ensure these settings use the `VITE_` prefix (e.g., `VITE_API_BASE_URL`).
* The Docker image contains an `entrypoint.sh` script that runs when the container starts, *before* the Nginx web server begins serving content.
* This `entrypoint.sh` script reads all environment variables within the container that start with `VITE_`.
* It then generates the `config.js` file (e.g., at `/usr/share/nginx/html/config.js`), writing the environment variables into the `window._env_` object within that file.
* Nginx serves this dynamically generated `config.js` along with the other static application files. The `index.html` loads it, providing the runtime configuration to the application via `window._env_`.

**Accessing Configuration in Code:**

The application's TypeScript code accesses these runtime variables via the global `window._env_` object. A helper function (e.g., in `src/config.ts`) is typically used to provide type safety and default values when reading from `window._env_`. Type definitions are provided (e.g., in `src/vite-env.d.ts`) to inform TypeScript about the structure of `window._env_`.

