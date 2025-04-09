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
import type { Folder as FolderType, Todo } from "@/services/backend/types";
import { useTodoTreeStore } from "@/store";
import { useFolderStore } from "@/store/folder.store";
import {
	AlertTriangle,
	ChevronDown,
	ChevronRight,
	Folder,
	MoreHorizontal,
	Plus,
	Trash,
} from "lucide-react";
import { useState } from "react";
import { TodoNode } from "./todo-node";

interface FolderNodeProps {
	folder?: FolderType;
	todos: Todo[];
	isExpanded: boolean;
	isSelected: boolean;
	isReadOnly?: boolean;
	isModerator?: boolean;
	isUncategorized?: boolean;
	onToggleExpand?: () => void;
	onAddTodo?: () => void;
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

export function FolderNode({
	folder,
	todos,
	isExpanded,
	isSelected,
	isReadOnly = false,
	isModerator = false,
	isUncategorized = false,
	onToggleExpand,
	onAddTodo,
}: FolderNodeProps) {
	const [showDeleteModal, setShowDeleteModal] = useState(false);
	const {
		toggleExpandFolder,
		setSelectedItem,
		selectedItemId,
		selectedItemType,
	} = useTodoTreeStore();
	const { deleteFolder } = useFolderStore();
	const { openAddTodoModal } = useTodoTreeStore();

	const handleDeleteConfirm = () => {
		if (!isUncategorized && folder) {
			deleteFolder(folder.id);
		}
		setShowDeleteModal(false);
	};

	const handleToggleExpand = () => {
		if (isUncategorized && onToggleExpand) {
			onToggleExpand();
		} else if (folder) {
			toggleExpandFolder(folder.id);
		}
	};

	const handleSelect = () => {
		if (isUncategorized) {
			setSelectedItem(-1, null);
			if (onToggleExpand) {
				onToggleExpand();
			}
		} else if (folder) {
			console.log("Selecting folder with ID:", folder.id);
			setSelectedItem(folder.id, "folder");
			toggleExpandFolder(folder.id);
		}
	};

	const handleAddTodo = (e: React.MouseEvent) => {
		e.stopPropagation();
		if (isUncategorized && onAddTodo) {
			onAddTodo();
		} else if (folder) {
			openAddTodoModal(folder.id, folder.name);
		}
	};

	const renderTodos = () => {
		if (!isExpanded) return null;

		return (
			<div className="pl-4 py-1">
				{todos.length === 0 ? (
					<div className="text-sm text-muted-foreground py-1 px-2">
						{isUncategorized ? "No uncategorized todos" : "Empty folder"}
					</div>
				) : (
					todos.map((todo) => (
						<TodoNode
							isModerator={isModerator}
							key={todo.id}
							todo={todo}
							isSelected={
								selectedItemType === "todo" && selectedItemId === todo.id
							}
							isReadOnly={isReadOnly}
						/>
					))
				)}
			</div>
		);
	};

	const folderName = isUncategorized ? "(-)" : folder?.name || "";

	return (
		<>
			<div className="relative flex items-center">
				<Button
					variant="ghost"
					size="icon"
					className="h-6 w-6 p-0 mr-1 flex-shrink-0"
					onClick={handleToggleExpand}
				>
					{isExpanded ? (
						<ChevronDown className="h-3.5 w-3.5 flex-shrink-0" />
					) : (
						<ChevronRight className="h-3.5 w-3.5 flex-shrink-0" />
					)}
					<span className="sr-only">
						{isExpanded ? "Collapse" : "Expand"}{" "}
						{isUncategorized ? "uncategorized todos" : "folder"}
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
					onClick={handleSelect}
				>
					<Folder className="mr-2 h-4 w-4 flex-shrink-0" />
					<span className="truncate">{folderName}</span>
				</Button>

				{!isReadOnly && (
					<>
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
								{!isReadOnly && (
									<DropdownMenuItem onClick={handleAddTodo}>
										<Plus className="mr-2 h-4 w-4" />
										<span>Add Todo</span>
									</DropdownMenuItem>
								)}
								{!isReadOnly && !isUncategorized && (
									<DropdownMenuItem
										onClick={(e) => {
											e.stopPropagation();
											setShowDeleteModal(true);
										}}
										className="!text-destructive [&_svg]:stroke-destructive"
									>
										<Trash className="mr-2 h-4 w-4" />
										<span>Delete</span>
									</DropdownMenuItem>
								)}
							</DropdownMenuContent>
						</DropdownMenu>
						{!isUncategorized && folder && (
							<DeleteFolderDialog
								isOpen={showDeleteModal}
								onClose={() => setShowDeleteModal(false)}
								onConfirm={handleDeleteConfirm}
								folderName={folderName}
							/>
						)}
					</>
				)}
			</div>

			{renderTodos()}
		</>
	);
}
