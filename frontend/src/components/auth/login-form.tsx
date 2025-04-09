"use client";

import type React from "react";

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuthStore } from "@/store/auth.store";
import { AlertCircle, ArrowRight, Lock, User } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";

export function LoginForm() {
	const [username, setUsername] = useState("");
	const [password, setPassword] = useState("");
	const navigate = useNavigate();

	// Get state and actions from auth store
	const { login, isLoading, error, clearError, isAuthenticated } =
		useAuthStore();

	// Monitor authentication state and redirect when authenticated
	useEffect(() => {
		if (isAuthenticated) {
			navigate("/todos");
		}
	}, [isAuthenticated, navigate]);

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		clearError();

		await login(username, password);
		// Redirection will be handled by the 'isAuthenticated' useEffect
	};

	return (
		<form onSubmit={handleSubmit} className="space-y-6">
			{error && (
				<Alert variant="destructive" className="animate-fadeIn">
					<AlertCircle className="h-4 w-4" />
					<AlertDescription>{error}</AlertDescription>
				</Alert>
			)}

			<div className="space-y-4">
				<div className="space-y-2">
					<Label htmlFor="username" className="text-sm font-medium">
						Username
					</Label>
					<div className="relative">
						<div className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
							<User className="h-5 w-5" />
						</div>
						<Input
							id="username"
							name="username"
							type="text"
							required
							value={username}
							onChange={(e) => setUsername(e.target.value)}
							className="pl-10 transition-all duration-200 focus:ring-2 focus:ring-purple-500"
							placeholder="Enter your username"
						/>
					</div>
				</div>

				<div className="space-y-2">
					<div className="flex items-center justify-between">
						<Label htmlFor="password" className="text-sm font-medium">
							Password
						</Label>
					</div>
					<div className="relative">
						<div className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
							<Lock className="h-5 w-5" />
						</div>
						<Input
							id="password"
							name="password"
							type="password"
							required
							value={password}
							onChange={(e) => setPassword(e.target.value)}
							className="pl-10 transition-all duration-200 focus:ring-2 focus:ring-purple-500"
							placeholder="Enter your password"
						/>
					</div>
				</div>
			</div>

			<div className="space-y-4">
				<Button
					type="submit"
					disabled={isLoading}
					className="w-full flex justify-center items-center gap-2 bg-purple-600 hover:bg-purple-700 text-white py-2 rounded-md transition-all duration-200 focus:ring-2 focus:ring-purple-500 focus:ring-offset-2"
				>
					{isLoading ? "Signing in..." : "Sign in"}
					{!isLoading && <ArrowRight className="h-4 w-4" />}
				</Button>

				<div className="text-center text-sm">
					Don't have an account?{" "}
					<Link
						to="/register"
						className="font-medium text-purple-600 hover:text-purple-500"
					>
						Register now
					</Link>
				</div>
			</div>
		</form>
	);
}
