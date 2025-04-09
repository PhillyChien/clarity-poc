import LoginPage from "@/page/login/login";
import RegisterPage from "@/page/register/register";
import TodosPage from "@/page/todos/todos";
import { Navigate, Route, Routes, useNavigate, useLocation } from "react-router";
import { ProtectedRoute } from "@/modules/auth/role.store";
import { useAuth } from "@/modules/auth";
import { useEffect } from "react";

// 添加 AuthListener 組件，用於在全局範圍監聽身份驗證狀態變化
function AuthListener() {
	const { isAuthenticated } = useAuth();
	const navigate = useNavigate();
	const location = useLocation();
	
	useEffect(() => {
		// 如果用戶未登錄且不在登錄或注冊頁面，則重定向到登錄頁面
		if (!isAuthenticated && 
			!location.pathname.includes('/login') && 
			!location.pathname.includes('/register')) {
			navigate('/login');
		}
	}, [isAuthenticated, navigate, location.pathname]);
	
	return null;
}

function App() {
	return (
		<>
			<AuthListener />
			<Routes>
				<Route path="/" element={<Navigate to="/login" />} />
				<Route path="/login" element={<LoginPage />} />
				<Route path="/register" element={<RegisterPage />} />
				<Route path="/todos" element={
					<ProtectedRoute>
						<TodosPage />
					</ProtectedRoute>
				} />
			</Routes>
		</>
	);
}

export default App;
