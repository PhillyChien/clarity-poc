"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useNavigate, Link } from "react-router"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertCircle, Lock, User, Mail, ArrowRight } from "lucide-react"
import { useAuthStore } from "@/store/auth.store"

export function RegisterForm() {
  const [username, setUsername] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [passwordError, setPasswordError] = useState("")
  const navigate = useNavigate()
  
  // Get state and actions from auth store
  const { register, isLoading, error, clearError, isAuthenticated } = useAuthStore()

  // Monitor authentication state and redirect when authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/todos")
    }
  }, [isAuthenticated, navigate])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    clearError()
    setPasswordError("")

    // Client-side password validation
    if (password !== confirmPassword) {
      setPasswordError("Passwords do not match")
      return
    }

    // Call register action - error handling is done within the store
    await register(username, email, password)
    // No need for try/catch or redirection here
    // Redirection will be handled by the useEffect above
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {(error || passwordError) && (
        <Alert variant="destructive" className="animate-fadeIn">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{passwordError || error}</AlertDescription>
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
              placeholder="Choose a username"
            />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="email" className="text-sm font-medium">
            Email
          </Label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
              <Mail className="h-5 w-5" />
            </div>
            <Input
              id="email"
              name="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="pl-10 transition-all duration-200 focus:ring-2 focus:ring-purple-500"
              placeholder="Enter your email"
            />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="password" className="text-sm font-medium">
            Password
          </Label>
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
              placeholder="Create a password"
            />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="confirmPassword" className="text-sm font-medium">
            Confirm Password
          </Label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
              <Lock className="h-5 w-5" />
            </div>
            <Input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              required
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="pl-10 transition-all duration-200 focus:ring-2 focus:ring-purple-500"
              placeholder="Confirm your password"
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
          {isLoading ? "Creating account..." : "Create account"}
          {!isLoading && <ArrowRight className="h-4 w-4" />}
        </Button>

        <div className="text-center text-sm">
          Already have an account?{" "}
          <Link to="/login" className="font-medium text-purple-600 hover:text-purple-500">
            Sign in
          </Link>
        </div>
      </div>
    </form>
  )
}
