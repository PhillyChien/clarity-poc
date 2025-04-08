"use client";

import { Button } from "@/components/ui/button";
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";
import type { Folder as FolderType } from "@/services/backend/types";
import {
	AlertTriangle,
	ChevronDown,
	ChevronRight,
	Folder,
	MoreHorizontal,
} from "lucide-react";
import { useState } from "react";

interface FolderItemProps {
	folder: FolderType;
	isExpanded: boolean;
	isSelected: boolean;
	onToggle: () => void;
	onSelect: () => void;
	onDelete: () => void;
}

function DeleteFolderDialog({
	isOpen,
	onClose,
	onConfirm,
	folderName,
}: {
	isOpen: boolean;
	onClose: () => void;
	onConfirm: () => void;
	folderName: string;
}) {
	return (
		<Dialog open={isOpen} onOpenChange={onClose}>
			<DialogContent className="sm:max-w-[425px]">
				<DialogHeader>
					<div className="flex items-center gap-2 text-destructive">
						<AlertTriangle className="h-5 w-5" />
						<DialogTitle>Delete Folder</DialogTitle>
					</div>
					<DialogDescription className="pt-2">
						Are you sure you want to delete{" "}
						<span className="font-medium">"{folderName}"</span>? This will also
						delete all todos inside this folder. This action cannot be undone.
					</DialogDescription>
				</DialogHeader>
				<DialogFooter className="gap-2 sm:justify-end">
					<Button variant="outline" onClick={onClose}>
						Cancel
					</Button>
					<Button variant="destructive" onClick={onConfirm}>
						Delete
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}

export function FolderItem({
	folder,
	isExpanded,
	isSelected,
	onToggle,
	onSelect,
	onDelete,
}: FolderItemProps) {
	const [showDeleteModal, setShowDeleteModal] = useState(false);

	const handleDeleteConfirm = () => {
		onDelete();
		setShowDeleteModal(false);
	};

	return (
		<div className="relative flex items-center">
			<Button
				variant="ghost"
				size="icon"
				className="h-6 w-6 p-0 mr-1 flex-shrink-0"
				onClick={onToggle}
			>
				{isExpanded ? (
					<ChevronDown className="h-3.5 w-3.5 flex-shrink-0" />
				) : (
					<ChevronRight className="h-3.5 w-3.5 flex-shrink-0" />
				)}
				<span className="sr-only">
					{isExpanded ? "Collapse" : "Expand"} folder
				</span>
			</Button>

			<Button
				variant="ghost"
				className={cn(
					"flex-1 justify-start rounded-md px-2 py-1 text-sm h-auto font-normal",
					isSelected
						? "bg-accent text-accent-foreground hover:bg-accent hover:text-accent-foreground"
						: "hover:bg-muted",
				)}
				onClick={onSelect}
			>
				<Folder className="mr-2 h-4 w-4 flex-shrink-0" />
				<span className="truncate">{folder.name}</span>
			</Button>

			<DropdownMenu>
				<DropdownMenuTrigger asChild>
					<Button
						variant="ghost"
						size="icon"
						className="h-6 w-6 p-0 opacity-70 ml-1 flex-shrink-0"
						onClick={(e) => e.stopPropagation()}
					>
						<MoreHorizontal className="h-3.5 w-3.5" />
						<span className="sr-only">Actions</span>
					</Button>
				</DropdownMenuTrigger>
				<DropdownMenuContent align="end">
					<DropdownMenuItem
						onClick={(e) => {
							e.stopPropagation();
							setShowDeleteModal(true);
						}}
						className="text-red-500"
					>
						<span>Delete</span>
					</DropdownMenuItem>
				</DropdownMenuContent>
			</DropdownMenu>

			<DeleteFolderDialog
				isOpen={showDeleteModal}
				onClose={() => setShowDeleteModal(false)}
				onConfirm={handleDeleteConfirm}
				folderName={folder.name}
			/>
		</div>
	);
}
