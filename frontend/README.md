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
        cp .env.example .env
        ```
    * Edit the `.env` file and ensure the variables are set correctly, especially the one pointing to your running backend API:
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


