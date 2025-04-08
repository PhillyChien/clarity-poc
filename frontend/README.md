# Frontend - Clarity POC Application

## 1. Introduction

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

## 2. Key Features

* **User Authentication:** Secure Login and Registration flows.
* **JWT Management:** Automatic handling of JWT to## 6. State Management (Zustand)

Global application state is managed using [Zustand](https://github.com/pmndrs/zustand), chosen for its simplicity, minimal boilerplate, and performance. State is organized into logical stores:

* **`authStore` (`src/store/auth.store.ts`)**
    * **Purpose:** Manages authentication status, the JWT token, and details of the currently logged-in user.
    * **State:** `isAuthenticated` (boolean), `user` (object containing username, roles, etc., or null), `token` (string or null), `isLoading` (boolean for login/register actions).
    * **Actions:** `login(credentials)`, `register(data)`, `logout()`, `setToken(token)`, `setUser(user)`, `loadUserFromToken()`. Actions typically interact with `authService`.

* **`folderStore` (`src/store/folder.store.ts`)**
    * **Purpose:** Manages the state related to user folders.
    * **State:** `folders` (array of Folder objects), `isLoading` (boolean), `error` (string or null).
    * **Actions:** `WorkspaceFolders()`, `addFolder(newFolderData)`, `updateFolder(folderId, updatedData)`, `deleteFolder(folderId)`. Actions interact with `folderService`.

* **`todoStore` (`src/store/todo.store.ts`)**
    * **Purpose:** Manages the state related to todos, typically filtered by the currently selected context (folder/filter).
    * **State:** `todos` (array of Todo objects relevant to the current view), `isLoading` (boolean), `error` (string or null).
    * **Actions:** `WorkspaceTodos(filterCriteria)`, `addTodo(newTodoData)`, `updateTodo(todoId, updatedData)`, `toggleTodoCompletion(todoId)`, `deleteTodo(todoId)`. Actions interact with `todoService`.

* **`uiStore` (`src/store/ui.store.ts`) (Recommended)**
    * **Purpose:** Manages state related purely to the UI, such as selections, modal visibility, etc., keeping data stores clean.
    * **State:** `selectedFolderId` (string | null | 'all'), `isTodoDetailModalOpen` (boolean), `selectedTodoIdForDetail` (string | null), `isCreateFolderModalOpen` (boolean).
    * **Actions:** `selectFolder(folderId)`, `openTodoDetailModal(todoId)`, `closeTodoDetailModal()`, `openCreateFolderModal()`, `closeCreateFolderModal()`.

Stores are designed to be atomic and focused. Components subscribe only to the stores and state slices they need. Actions within stores are responsible for calling the relevant service layer functions and updating the state accordingly.

## 7. Routing (React Router DOM)

Client-side routing is handled by [React Router](https://reactrouter.com/).

* **Route Definitions:** Routes are defined centrally (e.g., in `src/App.tsx` or a dedicated `src/Routes.tsx`) mapping URL paths (`/login`, `/todos`, `/admin/users`, etc.) to the corresponding page components (`LoginPage`, `TodoPage`, etc.).
* **Protected Routes:** A custom `ProtectedRoute` component is used to guard routes that require authentication.
    * It checks the authentication status (e.g., by reading `isAuthenticated` from `authStore`).
    * If the user is not authenticated, it redirects them to the `/login` page.
    * It can also incorporate **role-based access control**. For routes like `/admin/*` or `/moderator/*`, it checks the user's role (from `authStore`) and renders the requested component only if the role is authorized; otherwise, it might redirect to `/todos` or show an "Access Denied" message.
* **Navigation:** Programmatic navigation (e.g., after login) uses the `useNavigate` hook provided by React Router. Declarative navigation uses the `<Link>` component.

## 8. Getting Started (Local Development)

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
    # Using pnpm (recommended if lock file exists)
    pnpm install

    # Using npm
    # npm install

    # Using yarn
    # yarn install
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

* **Comprehensive Todo & Folder Management:**
    * Organize tasks within folders.
    * View folders in a tree-like or list structure.
    * Filter todos based on selected folders or predefined filters (e.g., "All Tasks").
    * Full CRUD (Create, Read, Update, Delete) operations for both folders and todos owned by the user.
    * Quick add functionality for todos.
    * (Optional) Detailed view/editing for individual todos.
* **Role-Based Access Control:** Specific views and functionalities restricted to Moderator and Super Admin roles.
    * Moderator: View all users' todos/folders, disable/enable todos.
    * Admin: View all users, promote users to Moderator.
* **Centralized API Communication:** A dedicated service layer manages all interactions with the backend API using the Fetch API.
* **Azure Clarity Integration:** Seamless integration of the Clarity tracking script for behavior analytics.
* **Responsive UI:** Designed to work effectively on different screen sizes using Tailwind CSS.

## 3. API Communication Layer

Interaction with the backend API is centralized within the `src/services/backend` directory to promote consistency, type safety, and maintainability. This pattern avoids scattering raw `Workspace` calls throughout the application.

* **Central API Client (`apiClient.ts` or similar):**
    * Encapsulates core request logic using the **Fetch API**.
    * Manages the backend API's base URL (from environment variables like `VITE_API_BASE_URL`).
    * Automatically attaches default headers (`Content-Type: application/json`, `Accept: application/json`).
    * Injects the `Authorization: Bearer <token>` header for authenticated requests, retrieving the token dynamically (e.g., from `authStore`).
    * Provides standardized wrapper functions for HTTP methods (GET, POST, PUT, DELETE).
    * Includes basic response handling (JSON parsing, HTTP status code checking, error throwing).

* **Service Modules (`authService.ts`, `folderService.ts`, etc.):**
    * Organized by API resource domain (e.g., authentication, folders, todos).
    * Import and utilize the central `apiClient`.
    * Define specific, typed functions for each backend operation (e.g., `loginUser(credentials): Promise<AuthResponse>`, `WorkspaceUserFolders(): Promise<Folder[]>`).

* **Usage:**
    * UI components or Zustand store actions import functions directly from these service modules (e.g., `import { fetchUserFolders } from '@/services/backend/folderService';`).
    * This decouples the UI/state logic from the specifics of the API communication.

## 4. Project Structure (Conceptual)

A recommended structure to organize the frontend codebase:

* `src/`: Main source code directory.
    * `pages/`: Components representing distinct application routes/pages (e.g., `LoginPage.tsx`, `TodoPage.tsx`, `UserManagementPage.tsx`).
    * `components/`: Reusable UI components, potentially organized by feature:
        * `folders/`: Components for folder display and interaction (e.g., `FolderTree.tsx`, `FolderItem.tsx`, `CreateFolderModal.tsx`).
        * `todos/`: Components for todo display and interaction (e.g., `TodoList.tsx`, `TodoItem.tsx`, `AddTodoForm.tsx`, `TodoDetailModal.tsx`).
        * `common/`: General reusable components (e.g., `Button.tsx`, `Modal.tsx`, `Input.tsx`, `Spinner.tsx`).
    * `layouts/`: Components defining the overall page structure (e.g., `MainLayout.tsx` integrating header, sidebar, content area; `AuthLayout.tsx` for login/register pages).
    * `store/`: Zustand store definitions and associated actions/types (e.g., `auth.store.ts`, `folder.store.ts`, `todo.store.ts`, `ui.store.ts`).
    * `services/`: Modules for interacting with external services.
        * `backend/`: Logic for communicating with the project's backend API.
            * `apiClient.ts`: Central Fetch API wrapper.
            * `authService.ts`: Authentication API functions.
            * `folderService.ts`: Folder API functions.
            * `todoService.ts`: Todo API functions.
            * `adminService.ts`: Admin API functions.
            * `moderatorService.ts`: Moderator API functions.
            * `types.ts`: Shared types/interfaces for API requests/responses.
    * `hooks/`: Custom React hooks (e.g., for UI interactions, non-data fetching logic).
    * `lib/`: Utility functions, constants, shared type definitions (non-API related).
    * `assets/`: Static assets (images, fonts, etc.).
* `public/`: Static assets served directly by the web server.
* `.env`, `.env.example`: Environment variable configuration.
* `biome.json`: Biome linter/formatter configuration.
* `tailwind.config.js`, `postcss.config.js`: Tailwind CSS configuration.
* `tsconfig.json`: TypeScript configuration.
* `vite.config.ts` (or similar): Build tool configuration.

## 5. Pages/Views Definitions

This section outlines the primary pages (corresponding to routes) and significant view components planned for the application frontend.

### 5.1 Public Pages (No Authentication Required)

* **`/login` (Component: `LoginPage.tsx`)**
    * **Purpose:** Provides the interface for existing users to authenticate.
    * **Layout:** Typically uses an `AuthLayout` (centered form).
    * **Key Components:** Login form (`LoginForm.tsx`) with fields for username/password, submit button, link to the registration page, error message display area.
    * **Functionality:**
        * Captures user input.
        * On submit, calls the `loginUser` function from `authService`.
        * Handles loading state during the API call.
        * On success: Updates `authStore` (token, user details), redirects the user to `/todos`.
        * On failure: Displays appropriate error messages received from the service layer.

* **`/register` (Component: `RegisterPage.tsx`)**
    * **Purpose:** Allows new users to create an account.
    * **Layout:** Uses `AuthLayout`.
    * **Key Components:** Registration form (`RegisterForm.tsx`) with fields for username, password, password confirmation, submit button, link to the login page, error display area.
    * **Functionality:**
        * Captures user input and performs basic client-side validation (e.g., password match).
        * On submit, calls the `registerUser` function from `authService`.
        * Handles loading state.
        * On success: Typically redirects the user to `/login` with a success message, or could potentially auto-login and redirect to `/todos`.
        * On failure: Displays relevant error messages (e.g., "Username already exists").

### 5.2 Authenticated Core Pages (Requires Login)

* **`/todos` (Component: `TodoPage.tsx`, Layout: `MainLayout.tsx`)**
    * **Purpose:** The primary workspace for users to manage their personal tasks (Todos) and organize them using Folders.
    * **Layout (`MainLayout.tsx`):** Defines the overall structure, usually including:
        * `Header`: Displays application logo/name, user information (dropdown?), logout button.
        * `Sidebar`: Contains the `FolderTree` component for navigation/filtering.
        * `Main Content Area`: Renders the `TodoList` based on the sidebar selection.
    * **Functionality (`TodoPage.tsx`):**
        * Acts as the container orchestrating the main components (`FolderTree`, `TodoList`).
        * Ensures user is authenticated using `ProtectedRoute` logic.
        * Initiates fetching of initial data (folders, potentially default todos) upon loading, likely dispatching actions handled by Zustand stores which call the respective services.
        * Manages the overall loading/error state for the page if initial data fetch fails.
        * Responds to folder selection events triggered from the `FolderTree` to update the `TodoList` view.

    ---
    * **Folder Management (Component: `FolderTree.tsx` - within Sidebar)**
        * **Purpose:** Displays user folders in a list or tree, allowing selection to filter todos. Includes options to manage folders.
        * **Key Components:**
            * Optional static filter items (e.g., "All Tasks", "Today" - requires backend support or client-side logic).
            * List of `FolderItem.tsx` components representing user folders.
            * Button/Icon to trigger adding a new folder (likely opens `CreateFolderModal.tsx`).
            * Visual indication (highlighting) of the currently selected folder/filter.
        * **Functionality:**
            * Subscribes to `folderStore` to get the list of folders.
            * Handles user clicks on folder items to update the selected folder state (likely in `uiStore`).
            * Provides actions (e.g., via context menu on `FolderItem`) for renaming or deleting folders, calling corresponding functions in `folderService` and updating `folderStore`.
            * Displays loading/error state if fetching folders fails.

    ---
    * **Todo Display Area (Component: `TodoList.tsx` - Main Content Area)**
        * **Purpose:** Renders the list of todos relevant to the selected folder/filter. Provides means to add new todos.
        * **Key Components:**
            * Header displaying the name of the selected folder/filter.
            * List container mapping over todo data to render `TodoItem.tsx` components.
            * `AddTodoForm.tsx` component for creating new tasks.
            * (Optional) Controls for sorting/filtering the list (e.g., dropdowns, buttons).
            * Clear visual states for: Loading todos, No todos found ("Empty state" message/graphic), Error fetching todos.
        * **Functionality:**
            * Subscribes to the selected folder/filter state (from `uiStore`).
            * Fetches the relevant list of todos via `todoService` based on the selection (updates `todoStore`).
            * Subscribes to `todoStore` to display the current list of todos.
            * Implements client-side sorting/filtering if added.

    ---
    * **Single Todo Item (Component: `TodoItem.tsx` - rendered by `TodoList`)**
        * **Purpose:** Displays a single todo and allows basic interactions.
        * **Key Components:** Checkbox (bound to completion status), Todo title display, indicators for due date/priority (if implemented), Action buttons/icons (e.g., Edit, Delete) often appearing on hover/focus.
        * **Functionality:**
            * Receives todo data as props.
            * Handles checkbox clicks to toggle completion status (calls `updateTodo` or a dedicated status update function in `todoService`, updates `todoStore`).
            * Triggers deletion (calls `deleteTodo` in `todoService` after confirmation, updates `todoStore`).
            * Triggers opening the `TodoDetailModal` for comprehensive editing.

    ---
    * **Add Todo Form (Component: `AddTodoForm.tsx` - within `TodoList` or Modal)**
        * **Purpose:** Enables quick creation of new todos within the context of the currently selected folder.
        * **Key Components:** Text input for the todo title, "Add Task" / Submit button. Could be a persistent input field or triggered by a button.
        * **Functionality:**
            * Captures title input.
            * On submit: calls `createTodo` from `todoService` (passing the title and the current folder context), handles loading state, clears the input on success, updates `todoStore`.

    ---
    * **(Optional) Todo Detail View/Modal (Component: `TodoDetailModal.tsx`)**
        * **Purpose:** Provides a dedicated interface for viewing and editing all details of a selected todo.
        * **Key Components:** Modal dialog containing form fields for title, description (textarea), due date picker, folder selector (to move task), potentially priority, etc. Save, Cancel, Delete buttons.
        * **Functionality:**
            * Opens when triggered from `TodoItem`.
            * Receives the selected todo's data and populates the form.
            * Handles form input changes.
            * On Save: calls `updateTodo` from `todoService` with all modified data, handles loading state, closes modal and updates `todoStore` on success.
            * Handles deletion from within the modal as well.

### 5.3 Authenticated Admin Pages (Requires 'Super Admin' Role)

* **`/admin/users` (Component: `UserManagementPage.tsx`)**
    * **Purpose:** Interface for administrators to view all registered users and manage their roles.
    * **Layout:** Uses `MainLayout`.
    * **Key Components:** A table or list (`UserList.tsx`) displaying usernames and roles. Buttons or actions next to `Normal` users to trigger promotion to `Moderator`.
    * **Functionality:**
        * Protected route ensuring only users with `Super Admin` role can access.
        * Fetches the list of all users via `adminService`.
        * Displays user data.
        * Handles clicks on the "Promote" action, calling the relevant function in `adminService`, and updating the UI/state on success.
        * Manages loading/error states for the user list.

### 5.4 Authenticated Moderator Pages (Requires 'Moderator' or 'Super Admin' Role)

* **`/moderator/view-all` (Component: `ModeratorViewPage.tsx`)**
    * **Purpose:** Allows privileged users (Moderators, Admins) to view content across all users and perform moderation actions (disabling todos).
    * **Layout:** Uses `MainLayout`. Might use tabs or sections to separate different views (e.g., All Todos).
    * **Key Components:** A table or list (`AllTodosList.tsx`) displaying todos from all users, clearly indicating the owner. Includes "Disable" / "Enable" buttons/toggles next to each todo, visible only to authorized roles.
    * **Functionality:**
        * Protected route ensuring only `Moderator` or `Super Admin` roles can access.
        * Fetches the list of all todos via `moderatorService`.
        * Displays the combined list, potentially with pagination if large.
        * Handles clicks on "Disable"/"Enable" actions, calling the relevant function in `moderatorService`, and updating the UI/state to reflect the change.
        * Manages loading/error states.

### 5.5 Utility Pages

* **`*` (Catch-all Route, Component: `NotFoundPage.tsx`)**
    * **Purpose:** Informs the user that the requested URL path does not match any defined routes.
    * **Layout:** Can use `MainLayout` or a simpler layout.
    * **Key Components:** A clear "404 - Page Not Found" message, potentially a graphic, a link back to the main application page (`/todos`) or the login page (`/login`) if the user appears logged out.
    * **Functionality:** Rendered by React Router when no other route matches.

## 6. State Management (Zustand)

Global application state is managed using [Zustand](https://github.com/pmndrs/zustand), chosen for its simplicity, minimal boilerplate, and performance. State is organized into logical stores:

* **`authStore` (`src/store/auth.store.ts`)**
    * **Purpose:** Manages authentication status, the JWT token, and details of the currently logged-in user.
    * **State:** `isAuthenticated` (boolean), `user` (object containing username, roles, etc., or null), `token` (string or null), `isLoading` (boolean for login/register actions).
    * **Actions:** `login(credentials)`, `register(data)`, `logout()`, `setToken(token)`, `setUser(user)`, `loadUserFromToken()`. Actions typically interact with `authService`.

* **`folderStore` (`src/store/folder.store.ts`)**
    * **Purpose:** Manages the state related to user folders.
    * **State:** `folders` (array of Folder objects), `isLoading` (boolean), `error` (string or null).
    * **Actions:** `WorkspaceFolders()`, `addFolder(newFolderData)`, `updateFolder(folderId, updatedData)`, `deleteFolder(folderId)`. Actions interact with `folderService`.

* **`todoStore` (`src/store/todo.store.ts`)**
    * **Purpose:** Manages the state related to todos, typically filtered by the currently selected context (folder/filter).
    * **State:** `todos` (array of Todo objects relevant to the current view), `isLoading` (boolean), `error` (string or null).
    * **Actions:** `WorkspaceTodos(filterCriteria)`, `addTodo(newTodoData)`, `updateTodo(todoId, updatedData)`, `toggleTodoCompletion(todoId)`, `deleteTodo(todoId)`. Actions interact with `todoService`.

* **`uiStore` (`src/store/ui.store.ts`) (Recommended)**
    * **Purpose:** Manages state related purely to the UI, such as selections, modal visibility, etc., keeping data stores clean.
    * **State:** `selectedFolderId` (string | null | 'all'), `isTodoDetailModalOpen` (boolean), `selectedTodoIdForDetail` (string | null), `isCreateFolderModalOpen` (boolean).
    * **Actions:** `selectFolder(folderId)`, `openTodoDetailModal(todoId)`, `closeTodoDetailModal()`, `openCreateFolderModal()`, `closeCreateFolderModal()`.

Stores are designed to be atomic and focused. Components subscribe only to the stores and state slices they need. Actions within stores are responsible for calling the relevant service layer functions and updating the state accordingly.

## 7. Routing (React Router)

Client-side routing is handled by [React Router](https://reactrouter.com/).

* **Route Definitions:** Routes are defined centrally (e.g., in `src/App.tsx` or a dedicated `src/Routes.tsx`) mapping URL paths (`/login`, `/todos`, `/admin/users`, etc.) to the corresponding page components (`LoginPage`, `TodoPage`, etc.).
* **Protected Routes:** A custom `ProtectedRoute` component is used to guard routes that require authentication.
    * It checks the authentication status (e.g., by reading `isAuthenticated` from `authStore`).
    * If the user is not authenticated, it redirects them to the `/login` page.
    * It can also incorporate **role-based access control**. For routes like `/admin/*` or `/moderator/*`, it checks the user's role (from `authStore`) and renders the requested component only if the role is authorized; otherwise, it might redirect to `/todos` or show an "Access Denied" message.
* **Navigation:** Programmatic navigation (e.g., after login) uses the `useNavigate` hook provided by React Router. Declarative navigation uses the `<Link>` component.
