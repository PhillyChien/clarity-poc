"use client";

import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { useTodoTreeStore } from "@/store";
import { useUsersStore } from "@/store/users.store";
import type { UserResponse } from "@/services/backend/types";
import { Users } from "lucide-react";
import { useEffect } from "react";

interface UserListSidebarProps {
	className?: string;
	onUserSelect?: (userId: number) => void;
	onClearSelection?: () => void;
}

export function UserListSidebar({
	className,
	onUserSelect,
	onClearSelection,
}: UserListSidebarProps) {
	const { users, isLoading, error, fetchAllUsers } = useUsersStore();

	const { selectedUserId, setSelectedUser, setSelectedItem } =
		useTodoTreeStore();

	// Fetch all users on component mount
	useEffect(() => {
		fetchAllUsers();
	}, [fetchAllUsers]);

	// Handle selecting a user
	const handleSelectUser = (userId: number | null) => {
		console.log("Selecting user with ID:", userId);
		// Clear the current selection
		setSelectedItem(null, null);

		// Set the selected user
		setSelectedUser(userId);

		// Call the external callback
		if (userId !== null && onUserSelect) {
			onUserSelect(userId);
		} else if (userId === null && onClearSelection) {
			onClearSelection();
		}
	};

	if (isLoading && users.length === 0) {
		return (
			<div className={cn("p-4 w-52", className)}>
				<div className="flex items-center mb-4">
					<Users className="h-5 w-5 mr-2 text-purple-500" />
					<h3 className="font-medium">Users</h3>
				</div>
				<p className="text-sm text-muted-foreground">Loading users...</p>
			</div>
		);
	}

	if (error) {
		return (
			<div className={cn("p-4 w-52", className)}>
				<div className="flex items-center mb-4">
					<Users className="h-5 w-5 mr-2 text-purple-500" />
					<h3 className="font-medium">Users</h3>
				</div>
				<p className="text-sm text-red-500">{error}</p>
			</div>
		);
	}

	return (
		<div className={cn("p-4 border-r h-full w-52", className)}>
			<div className="flex items-center mb-4">
				<Users className="h-5 w-5 mr-2 text-purple-500" />
				<h3 className="font-medium">Users</h3>
			</div>

			<div className="space-y-1">
				{users.map((user: UserResponse) => (
					<Button
						key={user.id}
						variant="ghost"
						className={cn(
							"w-full justify-start text-sm font-normal",
							selectedUserId === user.id && "bg-accent text-accent-foreground",
						)}
						onClick={() => handleSelectUser(user.id)}
					>
						<Avatar className="h-6 w-6 mr-2">
							<AvatarFallback
								className={cn(
									"text-xs",
									user.id === selectedUserId && "bg-purple-100 text-purple-700",
								)}
							>
								{user.username.charAt(0).toUpperCase()}
							</AvatarFallback>
						</Avatar>
						<span>{user.username}</span>
					</Button>
				))}
			</div>
		</div>
	);
}
