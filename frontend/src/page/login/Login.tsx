import { AuthLayout } from "@/components/auth/auth-layout";
import { LoginForm } from "@/components/auth/login-form";

export default function LoginPage() {
	return (
		<AuthLayout
			title="Sign in to your account"
			description="Enter your credentials to access your account"
		>
			<LoginForm />
		</AuthLayout>
	);
}
