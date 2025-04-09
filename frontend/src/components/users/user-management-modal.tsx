"use client";

import {
	Dialog,
	DialogContent,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog";
import {
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { useUsersStore } from "@/store/users.store";
import { useEffect, useState } from "react";
import { usePermission } from "@/modules/auth";
import type { UserResponse } from "@/services/backend/types";
import { cn } from "@/lib/utils";

interface UserManagementModalProps {
	isOpen: boolean;
	onClose: () => void;
}

export function UserManagementModal({
	isOpen,
	onClose,
}: UserManagementModalProps) {
	const { users, isLoading, error, fetchAllUsers } = useUsersStore();
	const { hasPermission } = usePermission();
	const canManageUsers = hasPermission("users.manage");

	// Fetch all users when the modal opens
	useEffect(() => {
		if (isOpen) {
			fetchAllUsers();
		}
	}, [isOpen, fetchAllUsers]);

	return (
		<Dialog open={isOpen} onOpenChange={onClose}>
			<DialogContent className="sm:max-w-[700px] h-[75vh] flex flex-col">
				<DialogHeader>
					<DialogTitle>User Management</DialogTitle>
				</DialogHeader>
				
				<div className="py-4 flex-1 overflow-y-auto">
					{isLoading ? (
						<p className="text-sm text-muted-foreground">Loading users...</p>
					) : error ? (
						<p className="text-sm text-red-500">{error}</p>
					) : (
						<div className="rounded-md border">
							<Table>
								<TableHeader>
									<TableRow>
										<TableHead className="sticky top-0 bg-white">User</TableHead>
										<TableHead className="sticky top-0 bg-white">Email</TableHead>
										<TableHead className="sticky top-0 bg-white">Role</TableHead>
										{canManageUsers && (
											<TableHead className="sticky top-0 bg-white">Actions</TableHead>
										)}
									</TableRow>
								</TableHeader>
								<TableBody>
									{users.map((user: UserResponse) => (
										<TableRow key={user.id}>
											<TableCell className="font-medium">{user.username}</TableCell>
											<TableCell>{user.email}</TableCell>
											<TableCell>
												<RoleBadge role={user.role} />
											</TableCell>
											{canManageUsers && (
												<TableCell>
													<RoleSelector user={user} />
												</TableCell>
											)}
										</TableRow>
									))}
								</TableBody>
							</Table>
						</div>
					)}
				</div>
			</DialogContent>
		</Dialog>
	);
}

// Role badge component
function RoleBadge({ role }: { role: string }) {
	const roleStyles = {
		SUPER_ADMIN: "bg-amber-50 text-amber-700 border-amber-200",
		MODERATOR: "bg-purple-50 text-purple-700 border-purple-200",
		NORMAL: "bg-blue-50 text-blue-700 border-blue-200",
	};

	const roleNames = {
		SUPER_ADMIN: "Super Admin",
		MODERATOR: "Moderator",
		NORMAL: "User",
	};

	const style = roleStyles[role as keyof typeof roleStyles] || "bg-gray-50 text-gray-700 border-gray-200";
	const displayName = roleNames[role as keyof typeof roleNames] || role;

	return (
		<span
			className={cn(
				"inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border",
				style
			)}
		>
			{displayName}
		</span>
	);
}

// Role selector component
function RoleSelector({ user }: { user: UserResponse }) {
	const { updateUserRole } = useUsersStore();
	const [isUpdating, setIsUpdating] = useState(false);
	
	// Don't allow changing SUPER_ADMIN roles or changing to SUPER_ADMIN
	if (user.role === "SUPER_ADMIN") {
		return <span className="text-sm text-muted-foreground">Cannot modify</span>;
	}
	
	const handleRoleChange = async (value: string) => {
		if (user.role === value) return;
		
		setIsUpdating(true);
		try {
			await updateUserRole(user.id, value);
		} catch (error) {
			console.error("Failed to update user role:", error);
		} finally {
			setIsUpdating(false);
		}
	};

	return (
		<Select
			disabled={isUpdating}
			defaultValue={user.role}
			onValueChange={handleRoleChange}
		>
			<SelectTrigger className="w-[130px]">
				<SelectValue placeholder="Change role" />
			</SelectTrigger>
			<SelectContent>
				<SelectItem value="NORMAL">User</SelectItem>
				<SelectItem value="MODERATOR">Moderator</SelectItem>
			</SelectContent>
		</Select>
	);
} 