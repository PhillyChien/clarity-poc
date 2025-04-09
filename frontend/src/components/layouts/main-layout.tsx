"use client";

import type React from "react";

import { Logo } from "@/components/logo";
import { Badge } from "@/components/ui/badge";
import { UserNav } from "@/components/user-nav";
import { useRole } from "@/store/role.store";
import { Crown, Shield } from "lucide-react";

interface MainLayoutProps {
	children: React.ReactNode;
	sidebar: React.ReactNode;
}

export function MainLayout({ children, sidebar }: MainLayoutProps) {
	const { isModerator, isAdmin } = useRole();
	
	return (
		<div className="flex h-screen flex-col">
			<header className="border-b bg-white">
				<div className="flex h-16 items-center justify-between px-6">
					<div className="flex items-center gap-6">
						<Logo />

						{/* Show admin badge if user is a super admin */}
						{isAdmin() && (
							<Badge
								variant="outline"
								className="bg-amber-50 text-amber-700 border-amber-200 flex items-center"
							>
								<Crown className="h-3 w-3 mr-1" />
								Admin View
							</Badge>
						)}
						
						{/* Show moderator badge if user is a moderator */}
						{isModerator() && (
							<Badge
								variant="outline"
								className="bg-purple-50 text-purple-700 border-purple-200 flex items-center"
							>
								<Shield className="h-3 w-3 mr-1" />
								Moderator View
							</Badge>
						)}
					</div>

					<div className="flex items-center gap-4">
						<UserNav />
					</div>
				</div>
			</header>
			<div className="flex flex-1 overflow-hidden">
				<aside className="border-r bg-gray-50 flex overflow-hidden">
					<div className="h-full overflow-y-auto flex">{sidebar}</div>
				</aside>
				<main className="flex-1 overflow-y-auto p-6">{children}</main>
			</div>
		</div>
	);
}
