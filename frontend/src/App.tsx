import { useAuth, ProtectedRoute } from "@/modules/auth";
import LoginPage from "@/page/login/login";
import RegisterPage from "@/page/register/register";
import TodosPage from "@/page/todos/todos";
import { useEffect } from "react";
import {
	Navigate,
	Route,
	Routes,
	useLocation,
	useNavigate,
} from "react-router";

import Clarity from "@microsoft/clarity";

// Add AuthListener component to listen for authentication status changes globally
function AuthListener() {
	const { isAuthenticated } = useAuth();
	const navigate = useNavigate();
	const location = useLocation();

	useEffect(() => {
		// If the user is not logged in and not on the login or registration page, redirect to the login page
		if (
			!isAuthenticated &&
			!location.pathname.includes("/login") &&
			!location.pathname.includes("/register")
		) {
			navigate("/login");
		}
	}, [isAuthenticated, navigate, location.pathname]);

	return null;
}

function App() {
	useEffect(() => {
		if (import.meta.env.PROD) {
			console.log("Initializing Clarity");
			Clarity.init("r66jd0jckz");
		} else {
			console.log("Not in production");
		}
	}, []);
	return (
		<>
			<AuthListener />
			<Routes>
				<Route path="/" element={<Navigate to="/login" />} />
				<Route path="/login" element={<LoginPage />} />
				<Route path="/register" element={<RegisterPage />} />
				<Route
					path="/todos"
					element={<ProtectedRoute />}
				>
					<Route index element={<TodosPage />} />
				</Route>
			</Routes>
		</>
	);
}

export default App;
