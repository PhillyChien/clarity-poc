import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuLabel,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useAuth, useRole } from "@/modules/auth";
import { useNavigate } from "react-router";

export function UserNav() {
	const { user, logout } = useAuth();
	const { currentRole } = useRole();
	const navigate = useNavigate();

	const handleLogout = async () => {
		await logout();
		// 使用 React Router 導航而不是直接修改 window.location.href
		navigate("/login");
	};

	if (!user) {
		return null;
	}

	// Get user initials for avatar
	const getInitials = (username: string) => {
		return username.charAt(0).toUpperCase();
	};

	return (
		<DropdownMenu>
			<DropdownMenuTrigger asChild>
				<Button variant="ghost" className="h-8 w-8 rounded-full">
					<Avatar className="h-8 w-8">
						<AvatarFallback>{getInitials(user.username)}</AvatarFallback>
					</Avatar>
				</Button>
			</DropdownMenuTrigger>
			<DropdownMenuContent align="end">
				<DropdownMenuLabel>
					<div className="flex flex-col space-y-1">
						<p className="text-sm font-medium">{user.username}</p>
						<p className="text-xs text-muted-foreground">{user.email}</p>
						{currentRole && (
							<p className="text-xs text-muted-foreground">Role: {currentRole}</p>
						)}
					</div>
				</DropdownMenuLabel>
				<DropdownMenuSeparator />
				<DropdownMenuItem onClick={handleLogout} className="cursor-pointer">
					Logout
				</DropdownMenuItem>
			</DropdownMenuContent>
		</DropdownMenu>
	);
}
