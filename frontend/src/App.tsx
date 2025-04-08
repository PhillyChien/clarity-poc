import LoginPage from "@/page/login/login";
import RegisterPage from "@/page/register/register";
import TodosPage from "@/page/todos/todos";
import { Navigate, Route, Routes } from "react-router";

function App() {
	return (
		<Routes>
			<Route path="/" element={<Navigate to="/login" />} />
			<Route path="/login" element={<LoginPage />} />
			<Route path="/register" element={<RegisterPage />} />
			<Route path="/todos" element={<TodosPage />} />
		</Routes>
	);
}

export default App;
