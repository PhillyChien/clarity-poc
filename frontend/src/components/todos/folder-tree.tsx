"use client";

import { FolderItem } from "@/components/todos/folder-item";
import { TodoTreeItem } from "@/components/todos/todo-tree-item";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { useFolderStore } from "@/store/folder.store";
import { useTodoStore } from "@/store/todo.store";
import { useUIStore } from "@/store/ui.store";
import { Folder, FolderPlus } from "lucide-react";
import { ChevronDown, ChevronRight } from "lucide-react";
import { useCallback, useEffect, useState } from "react";

interface FolderTreeProps {
	onAddFolderClick: () => void;
}

export function FolderTree({ onAddFolderClick }: FolderTreeProps) {
	// folder expanded state
	const [expandedFolders, setExpandedFolders] = useState<
		Record<string, boolean>
	>({});
	const [showUncategorizedTodos, setShowUncategorizedTodos] = useState(true);

	// get data and methods from store
	const { folders, fetchUserFolders, deleteFolder } = useFolderStore();
	const {
		todosByFolder,
		fetchTodosByFolder,
		deleteTodo,
		uncategorizedTodos,
		fetchUncategorizedTodos,
	} = useTodoStore();
	const { selectedFolderId, setSelectedFolder, todoDetailId, setTodoDetailId } =
		useUIStore();

	// toggle folder expanded state
	const toggleExpandFolder = useCallback((folderId: number) => {
		setExpandedFolders((prev) => ({
			...prev,
			[folderId.toString()]: !prev[folderId.toString()],
		}));
	}, []);

	// select folder
	const handleSelectFolder = useCallback(
		(folderId: number) => {
			setSelectedFolder(folderId);
			setTodoDetailId(null);
			fetchTodosByFolder(folderId);

			// auto expand selected folder
			toggleExpandFolder(folderId);
		},
		[
			setSelectedFolder,
			setTodoDetailId,
			fetchTodosByFolder,
			toggleExpandFolder,
		],
	);

	// select todo
	const handleSelectTodo = useCallback(
		(todoId: number) => {
			setTodoDetailId(todoId);
		},
		[setTodoDetailId],
	);

	// delete folder
	const handleDeleteFolder = useCallback(
		async (folderId: number) => {
			try {
				await deleteFolder(folderId);

				// if the deleted folder is the currently selected folder, select another folder
				if (selectedFolderId === folderId && folders.length > 0) {
					const otherFolderId = folders.find((f) => f.id !== folderId)?.id;
					if (otherFolderId) {
						handleSelectFolder(otherFolderId);
					}
				}
			} catch (error) {
				console.error("Failed to delete folder", error);
			}
		},
		[deleteFolder, folders, handleSelectFolder, selectedFolderId],
	);

	// toggle uncategorized todos display state
	const toggleUncategorizedTodos = useCallback(() => {
		setShowUncategorizedTodos((prev) => !prev);
	}, []);

	// initial load data
	useEffect(() => {
		// load folders
		fetchUserFolders();

		// load uncategorized todos
		fetchUncategorizedTodos();
	}, [fetchUserFolders, fetchUncategorizedTodos]);

	// select the first folder on initial load
	useEffect(() => {
		if (folders.length > 0 && !selectedFolderId) {
			handleSelectFolder(folders[0].id);
		}
	}, [folders, selectedFolderId, handleSelectFolder]);

	// render uncategorized todos section
	const renderUncategorizedSection = () => (
		<div>
			<div className="flex items-center mb-1">
				<Button
					variant="ghost"
					size="icon"
					onClick={toggleUncategorizedTodos}
					className="h-6 w-6 p-0 mr-1"
				>
					{showUncategorizedTodos ? (
						<ChevronDown className="h-3.5 w-3.5 flex-shrink-0" />
					) : (
						<ChevronRight className="h-3.5 w-3.5 flex-shrink-0" />
					)}
					<span className="sr-only">
						{showUncategorizedTodos ? "Collapse" : "Expand"} uncategorized todos
					</span>
				</Button>

				<Button
					variant="ghost"
					className={cn(
						"flex-1 justify-start rounded-md px-2 py-1 text-sm h-auto font-normal",
						"hover:bg-muted",
					)}
					onClick={toggleUncategorizedTodos}
				>
					<Folder className="mr-2 h-4 w-4 flex-shrink-0" />
					<span className="truncate">(-)</span>
				</Button>
			</div>

			{showUncategorizedTodos && (
				<div className="ml-6 mt-1 border-l pl-2 mb-2">
					{uncategorizedTodos.length > 0 ? (
						uncategorizedTodos.map((todo) => (
							<TodoTreeItem
								key={todo.id}
								todo={todo}
								isSelected={todoDetailId === todo.id}
								onSelect={() => handleSelectTodo(todo.id)}
								onDelete={() => deleteTodo(todo.id)}
							/>
						))
					) : (
						<p className="text-sm text-muted-foreground py-1 px-2">
							No uncategorized todos
						</p>
					)}
				</div>
			)}
		</div>
	);

	// render folder list
	const renderFolderList = () =>
		folders.map((folder) => {
			const folderTodos = todosByFolder[folder.id] || [];
			const isExpanded = expandedFolders[folder.id.toString()];
			const isSelected = selectedFolderId === folder.id;

			return (
				<div key={folder.id} className="mb-1">
					<FolderItem
						folder={folder}
						isExpanded={isExpanded}
						isSelected={isSelected}
						onToggle={() => toggleExpandFolder(folder.id)}
						onSelect={() => handleSelectFolder(folder.id)}
						onDelete={() => handleDeleteFolder(folder.id)}
					/>

					{isExpanded && (
						<div className="ml-6 mt-1 border-l pl-2">
							{folderTodos.length > 0 ? (
								folderTodos.map((todo) => (
									<TodoTreeItem
										key={todo.id}
										todo={todo}
										isSelected={todoDetailId === todo.id}
										onSelect={() => handleSelectTodo(todo.id)}
										onDelete={() => deleteTodo(todo.id)}
									/>
								))
							) : (
								<p className="text-sm text-muted-foreground py-1 px-2">
									Empty folder
								</p>
							)}
						</div>
					)}
				</div>
			);
		});

	return (
		<div className="space-y-4">
			{/* title and add button */}
			<div className="flex items-center justify-between">
				<h2 className="text-lg font-semibold">Folders</h2>
				<Button
					variant="ghost"
					size="sm"
					onClick={onAddFolderClick}
					className="h-8 w-8 p-0"
				>
					<FolderPlus className="h-5 w-5" />
					<span className="sr-only">Add folder</span>
				</Button>
			</div>

			{/* main content area */}
			<div>
				{/* uncategorized todos section */}
				{renderUncategorizedSection()}

				{/* folder list */}
				{folders.length === 0 ? (
					<p className="text-sm text-muted-foreground px-3 py-2">
						No folders yet. Click the + button to create one.
					</p>
				) : (
					renderFolderList()
				)}
			</div>
		</div>
	);
}
