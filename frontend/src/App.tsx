import { Navigate, Route, Routes } from "react-router"
import LoginPage from "@/page/login/Login"
import RegisterPage from "@/page/register/Register"

function App() {
	return <Routes>
		<Route path="/" element={<Navigate to="/login" />} />
		<Route path="/login" element={<LoginPage />} />
		<Route path="/register" element={<RegisterPage />} />
	</Routes>;
}

export default App;
