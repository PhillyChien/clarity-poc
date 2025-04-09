"use client";

import { TodoDetailForm } from "@/components/todos/todo-detail-form";
import { Button } from "@/components/ui/button";
import type { Folder, Todo, UpdateTodoRequest } from "@/services/backend/types";
import { useTodoStore, useTodoTreeStore } from "@/store";
import { useFolderStore } from "@/store/folder.store";
import { Eye, Plus } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Badge } from "../ui/badge";

interface ItemDetailViewProps {
	isViewingOtherUser?: boolean;
}

export function ItemDetailView({
	isViewingOtherUser = false,
}: ItemDetailViewProps) {
	const [isLoading, setIsLoading] = useState(false);
	const [viewMode, setViewMode] = useState<"read" | "edit">("read");

	// Store
	const { selectedItemId, selectedItemType, openAddTodoModal } =
		useTodoTreeStore();
	const { getTodo, updateTodo, todosByFolder, uncategorizedTodos } =
		useTodoStore();
	const { folders } = useFolderStore();

	// 使用 useMemo 根据 selectedItemId 和 selectedItemType 计算当前显示的项目
	const currentItem = useMemo(() => {
		// 如果没有选择项目，返回 null
		if (selectedItemId === null || selectedItemType === null) {
			return null;
		}

		// 如果选择的是 todo，查找对应的 todo
		if (selectedItemType === "todo") {
			// 先在所有文件夹中查找
			for (const [, todos] of Object.entries(todosByFolder)) {
				const found = todos.find((t) => t.id === selectedItemId);
				if (found) {
					return found;
				}
			}

			// 再在未分类列表中查找
			const found = uncategorizedTodos.find((t) => t.id === selectedItemId);
			if (found) {
				return found;
			}

			// 如果本地没有找到，会通过副作用加载
			return null;
		}

		// 如果选择的是文件夹，查找对应的文件夹
		if (selectedItemType === "folder") {
			return folders.find((f) => f.id === selectedItemId) || null;
		}

		return null;
	}, [
		selectedItemId,
		selectedItemType,
		todosByFolder,
		uncategorizedTodos,
		folders,
	]);

	// When the selected item changes, reset the view mode
	useEffect(() => {
		if (selectedItemId !== null) {
			setViewMode("read");
		}
	}, [selectedItemId]);

	// See other user's content, force read-only mode
	useEffect(() => {
		if (isViewingOtherUser) {
			setViewMode("read");
		}
	}, [isViewingOtherUser]);

	// Load the selected Todo from the server (if not found in local state)
	useEffect(() => {
		const loadTodoFromServer = async () => {
			// Only load from server if selected is a todo and not found in local state
			if (
				selectedItemType === "todo" &&
				selectedItemId !== null &&
				!currentItem
			) {
				setIsLoading(true);
				try {
					await getTodo(selectedItemId);
				} catch (error) {
					console.error("Failed to load todo details:", error);
				} finally {
					setIsLoading(false);
				}
			}
		};

		loadTodoFromServer();
	}, [selectedItemId, selectedItemType, currentItem, getTodo]);

	const handleUpdateTodo = async (todoId: number, data: UpdateTodoRequest) => {
		if (isViewingOtherUser) return;
		await updateTodo(todoId, data);

		// 更新后切换回阅读模式
		setViewMode("read");
	};

	if (isLoading) {
		return (
			<div className="flex h-full flex-col items-center justify-center">
				<div className="text-center">
					<h3 className="mb-2 text-xl font-medium">Loading...</h3>
				</div>
			</div>
		);
	}

	if (!currentItem) {
		return (
			<div className="flex h-full flex-col items-center justify-center">
				<div className="text-center">
					<h3 className="mb-2 text-xl font-medium">No Selection</h3>
					<p className="mb-6 text-gray-500">
						Select a folder or todo from the sidebar
					</p>
					{!isViewingOtherUser && (
						<Button onClick={() => openAddTodoModal()}>
							<Plus className="mr-2 h-4 w-4" />
							Create New Todo
						</Button>
					)}
				</div>
			</div>
		);
	}

	if (selectedItemType === "folder") {
		const folder = currentItem as Folder;
		return (
			<div>
				<div className="mb-6 flex items-center justify-between">
					<div>
						<h2 className="text-2xl font-bold">{folder.name}</h2>
						<p className="text-gray-500">Folder</p>
					</div>
					{!isViewingOtherUser && (
						<Button onClick={() => openAddTodoModal(folder.id, folder.name)}>
							<Plus className="mr-2 h-4 w-4" />
							Add Todo
						</Button>
					)}
					{isViewingOtherUser && (
						<Badge
							variant="outline"
							className="bg-yellow-50 text-yellow-800 border-yellow-200 flex items-center"
						>
							<Eye className="h-3 w-3 mr-1" />
							Read Only
						</Badge>
					)}
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

	if (selectedItemType === "todo") {
		const todo = currentItem as Todo;
		// 使用 key 属性确保 todo 状态变化时组件会重新渲染
		return (
			<div>
				<div className="mb-4 flex items-center justify-between">
					<div>
						<h2 className="text-xl font-bold">{todo.title}</h2>
						<p className="text-gray-500">Todo</p>
					</div>
					{!isViewingOtherUser && (
						<Button
							variant="outline"
							onClick={() => setViewMode(viewMode === "read" ? "edit" : "read")}
						>
							{viewMode === "read" ? "Edit" : "View"}
						</Button>
					)}
					{isViewingOtherUser && (
						<Badge
							variant="outline"
							className="bg-yellow-50 text-yellow-800 border-yellow-200 flex items-center"
						>
							<Eye className="h-3 w-3 mr-1" />
							Read Only
						</Badge>
					)}
				</div>

				<TodoDetailForm
					key={`${todo.id}-${viewMode}-${todo.completed}`}
					todo={todo}
					onUpdateTodo={(updatedTodo) => {
						handleUpdateTodo(todo.id, updatedTodo);
					}}
					folders={folders}
					mode={isViewingOtherUser ? "read" : viewMode}
				/>
			</div>
		);
	}

	return null;
}
