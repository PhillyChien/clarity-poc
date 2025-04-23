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
import { useAuth, useRole } from "@/modules/auth";
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
import { useCallback, useState } from "react";
import { TodoNode } from "./todo-node";

interface FolderNodeProps {
	folder: FolderType;
	todos: Todo[];
	isExpanded: boolean;
	isSelected: boolean;
	isUncategorized?: boolean;
	onToggleExpand?: () => void;
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
	isUncategorized = false,
	onToggleExpand,
}: FolderNodeProps) {
	const [showDeleteModal, setShowDeleteModal] = useState(false);
	const { toggleExpandFolder, setSelectedItem } = useTodoTreeStore();
	const { deleteFolder } = useFolderStore();
	const { openAddTodoModal } = useTodoTreeStore();

	// 使用权限系统
	const { hasPermission } = useRole();
	const { user } = useAuth();
	// 检查是否是文件夹所有者
	const isOwner = folder ? user?.id === folder.ownerId : false;

	// 检查权限
	// 只有所有者可以删除自己的文件夹，管理员和版主不能删除其他人的文件夹
	const canDeleteFolder = isOwner && hasPermission("folders.own.delete");
	// 检查是否有创建待办事项的权限
	const canCreateTodo = hasPermission("todos.own.create");
	// 只有文件夹的所有者才能在文件夹中添加待办事项
	// 对于未分类区域，只有当查看的是自己的内容时才能添加
	const canAddTodo = isOwner && canCreateTodo;

	// 根据权限判断是否为只读状态
	// 如果是所有者，则不是只读；如果不是所有者，则是只读状态
	const isReadOnly = !isOwner;

	const handleDeleteConfirm = () => {
		// 只有文件夹存在、不是未分类文件夹、是所有者、且有权限时才能删除
		if (!isUncategorized && folder && isOwner && canDeleteFolder) {
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

		if (isUncategorized) {
			if (canCreateTodo && isOwner) {
				openAddTodoModal(undefined, folder.name);
			}
			return;
		}

		if (folder && canAddTodo) {
			openAddTodoModal(folder.id, folder.name);
		}
	};

	const renderTodos = useCallback(() => {
		if (!isExpanded) {
			return null;
		}

		if (todos.length === 0) {
			if (isUncategorized) {
				return (
					<div className="pl-6 my-2">
						<p className="text-sm text-muted-foreground">
							No Uncategorized Todos
						</p>
					</div>
				);
			}

			return (
				<div className="pl-6 my-2">
					<p className="text-sm text-muted-foreground">Empty folder</p>
				</div>
			);
		}

		return (
			<div className="pl-6 mt-1">
				{todos.map((todo) => (
					<TodoNode key={todo.id} todo={todo} isReadOnly={isReadOnly} />
				))}
			</div>
		);
	}, [isExpanded, todos, isUncategorized, isReadOnly]);

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
								{canAddTodo && (
									<DropdownMenuItem onClick={handleAddTodo}>
										<Plus className="mr-2 h-4 w-4" />
										<span>Add Todo</span>
									</DropdownMenuItem>
								)}
								{canDeleteFolder && !isUncategorized && (
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
