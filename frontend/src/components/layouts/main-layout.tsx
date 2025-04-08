"use client";

import type React from "react";

import { Logo } from "@/components/logo";
import { UserNav } from "@/components/user-nav";

interface MainLayoutProps {
	children: React.ReactNode;
	sidebar: React.ReactNode;
}

export function MainLayout({ children, sidebar }: MainLayoutProps) {
	return (
		<div className="flex h-screen flex-col">
			<header className="border-b bg-white">
				<div className="flex h-16 items-center justify-between px-6">
					<Logo />
					<div className="flex items-center gap-4">
						<UserNav />
					</div>
				</div>
			</header>
			<div className="flex flex-1 overflow-hidden">
				<aside className="w-64 border-r bg-gray-50">
					<div className="h-full overflow-y-auto p-4">{sidebar}</div>
				</aside>
				<main className="flex-1 overflow-y-auto p-6">{children}</main>
			</div>
		</div>
	);
}
