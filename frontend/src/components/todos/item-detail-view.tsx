"use client";

import { TodoDetailForm } from "@/components/todos/todo-detail-form";
import { Button } from "@/components/ui/button";
import type { Folder, Todo } from "@/services/backend/types";
import { useFolderStore } from "@/store/folder.store";
import { useTodoStore } from "@/store/todo.store";
import { useUIStore } from "@/store/ui.store";
import { Plus } from "lucide-react";
import { useEffect, useState } from "react";

interface ItemDetailViewProps {
	onAddTodo: () => void;
}

export function ItemDetailView({ onAddTodo }: ItemDetailViewProps) {
	const { folders } = useFolderStore();
	const { getCurrentTodos, updateTodo, getTodo } = useTodoStore();
	const { selectedFolderId, todoDetailId } = useUIStore();

	const [selectedItem, setSelectedItem] = useState<Folder | Todo | null>(null);
	const [itemType, setItemType] = useState<"folder" | "todo" | null>(null);
	const [isLoading, setIsLoading] = useState(false);

	useEffect(() => {
		// when todoDetailId changes, force clear the previous selection, ensure UI re-renders
		if (todoDetailId !== null) {
			setSelectedItem(null);
			setItemType(null);
		}
	}, [todoDetailId]);

	useEffect(() => {
		const loadData = async () => {
			// If a todo is selected (prioritize todo selection)
			if (todoDetailId) {
				setIsLoading(true);
				try {
					// First check if we already have the todo in our local state
					const currentTodos = getCurrentTodos();
					let todo = currentTodos.find((t) => t.id === todoDetailId);

					// If not, fetch it from the server
					if (!todo) {
						todo = await getTodo(todoDetailId);
					}

					if (todo) {
						setSelectedItem(todo);
						setItemType("todo");
					} else {
						// Reset if todo not found
						setSelectedItem(null);
						setItemType(null);
					}
				} catch (error) {
					console.error("Failed to load todo details:", error);
					setSelectedItem(null);
					setItemType(null);
				} finally {
					setIsLoading(false);
				}
				return;
			}

			// If a folder is selected
			if (selectedFolderId) {
				const folder = folders.find((f) => f.id === selectedFolderId);
				if (folder) {
					setSelectedItem(folder);
					setItemType("folder");
					return;
				}
			}

			// No selection
			setSelectedItem(null);
			setItemType(null);
		};

		loadData();
	}, [selectedFolderId, todoDetailId, folders, getCurrentTodos, getTodo]);

	if (isLoading) {
		return (
			<div className="flex h-full flex-col items-center justify-center">
				<div className="text-center">
					<h3 className="mb-2 text-xl font-medium">Loading...</h3>
				</div>
			</div>
		);
	}

	if (!selectedItem) {
		return (
			<div className="flex h-full flex-col items-center justify-center">
				<div className="text-center">
					<h3 className="mb-2 text-xl font-medium">No Selection</h3>
					<p className="mb-6 text-gray-500">
						Select a folder or todo from the sidebar
					</p>
					<Button onClick={onAddTodo}>
						<Plus className="mr-2 h-4 w-4" />
						Create New Todo
					</Button>
				</div>
			</div>
		);
	}

	if (itemType === "folder") {
		const folder = selectedItem as Folder;
		return (
			<div>
				<div className="mb-6 flex items-center justify-between">
					<div>
						<h2 className="text-2xl font-bold">{folder.name}</h2>
						<p className="text-gray-500">Folder</p>
					</div>
					<Button onClick={onAddTodo}>
						<Plus className="mr-2 h-4 w-4" />
						Add Todo
					</Button>
				</div>

				<div className="rounded-lg border p-4">
					<h3 className="mb-2 text-lg font-medium">About</h3>
					<p className="text-gray-600">
						{folder.description || "No description"}
					</p>
				</div>
			</div>
		);
	}

	if (itemType === "todo") {
		const todo = selectedItem as Todo;
		// use key property to ensure TodoDetailForm component is recreated when todo changes
		return (
			<TodoDetailForm
				key={todo.id}
				todo={todo}
				onUpdateTodo={(updatedTodo) => updateTodo(todo.id, updatedTodo)}
				folders={folders}
			/>
		);
	}

	return null;
}
