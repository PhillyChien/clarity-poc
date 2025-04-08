import { AuthLayout } from "@/components/auth/auth-layout";
import { RegisterForm } from "@/components/auth/register-form";

export default function RegisterPage() {
	return (
		<AuthLayout
			title="Create an account"
			description="Fill in the details below to create your account"
		>
			<RegisterForm />
		</AuthLayout>
	);
}
