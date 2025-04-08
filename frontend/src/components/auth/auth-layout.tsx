import type React from "react";

interface AuthLayoutProps {
	children: React.ReactNode;
	title: string;
	description?: string;
}

export function AuthLayout({ children, title, description }: AuthLayoutProps) {
	return (
		<div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 px-4 py-12 sm:px-6">
			<div className="w-full max-w-md space-y-8 rounded-xl bg-white p-8 shadow-sm">
				<div className="relative">
					<div className="absolute -top-1 -left-6 h-12 w-12 rounded-xl bg-purple-200/60" />
					<div className="absolute -top-1 left-2 h-8 w-8 rounded-xl bg-teal-200/80" />
					<h2 className="relative mt-6 text-3xl font-extrabold tracking-tight text-gray-900">
						{title}
					</h2>
				</div>
				{description && (
					<p className="mt-3 text-sm text-gray-600">{description}</p>
				)}
				<div className="mt-8">{children}</div>
			</div>
		</div>
	);
}
