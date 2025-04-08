"use client";

import { Button } from "@/components/ui/button";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils";
import type { Todo } from "@/services/backend/types";
import { useTodoStore } from "@/store/todo.store";
import { useUIStore } from "@/store/ui.store";
import { CheckCircle, Circle, MoreHorizontal, Trash } from "lucide-react";
import { memo, useCallback, useState } from "react";

interface TodoTreeItemProps {
	todo: Todo;
	isSelected?: boolean;
	onSelect?: () => void;
	onDelete?: () => void;
}

export const TodoTreeItem = memo(function TodoTreeItem({
	todo,
	isSelected: propIsSelected,
	onSelect,
	onDelete,
}: TodoTreeItemProps) {
	const { toggleTodoCompletion, getCurrentTodos, uncategorizedTodos } = useTodoStore();
	const { todoDetailId } = useUIStore();
	const [isLoading, setIsLoading] = useState(false);

	const isSelected = propIsSelected ?? todoDetailId === todo.id;

	const handleToggleComplete = useCallback(
		async (e: React.MouseEvent) => {
			e.stopPropagation();
			if (isLoading) return; // 防止重复点击
			
			setIsLoading(true);
			try {
				await toggleTodoCompletion(todo.id);
				console.log(`Toggled todo completion for ID ${todo.id}`);
			} catch (error) {
				console.error("Failed to toggle todo completion:", error);
			} finally {
				setIsLoading(false);
			}
		},
		[todo.id, toggleTodoCompletion, isLoading],
	);

	// get latest todo state, avoid using outdated todo prop
	const getLatestTodoState = useCallback(() => {
		// 首先从当前文件夹中查找
		const currentTodos = getCurrentTodos();
		const todoFromCurrent = currentTodos.find((t) => t.id === todo.id);
		if (todoFromCurrent) return todoFromCurrent;
		
		// 然后从未分类列表中查找
		const todoFromUncategorized = uncategorizedTodos.find((t) => t.id === todo.id);
		if (todoFromUncategorized) return todoFromUncategorized;
		
		// 如果都找不到，返回传入的原始todo
		return todo;
	}, [getCurrentTodos, uncategorizedTodos, todo]);

	const latestTodo = getLatestTodoState();
	const isCompleted = latestTodo.completed;

	return (
		<div
			className={cn(
				"flex items-center w-full p-1 rounded-md relative gap-2",
				isSelected ? "bg-accent" : "hover:bg-muted",
			)}
		>
			<Button
				type="button"
				variant="ghost"
				size="icon"
				className={cn(
					"h-6 w-6 p-0 flex-shrink-0 z-10",
					isLoading && "opacity-50 cursor-wait"
				)}
				onClick={handleToggleComplete}
				disabled={isLoading}
			>
				{isCompleted ? (
					<CheckCircle className="h-4 w-4 text-green-500" />
				) : (
					<Circle className="h-4 w-4 text-gray-400" />
				)}
				<span className="sr-only">
					{isCompleted ? "Mark as incomplete" : "Mark as complete"}
				</span>
			</Button>

			<Button
				type="button"
				variant="ghost"
				className={cn(
					"flex-1 h-auto py-1 px-0 justify-start text-left font-normal",
					isCompleted && "text-gray-500",
				)}
				onClick={onSelect}
			>
				<span className={cn("text-sm truncate", isCompleted && "line-through")}>
					{latestTodo.title}
				</span>
			</Button>

			<DropdownMenu>
				<DropdownMenuTrigger asChild>
					<Button
						variant="ghost"
						size="icon"
						className="h-6 w-6 p-0 opacity-50 hover:opacity-100 flex-shrink-0 z-10"
						onClick={(e) => e.stopPropagation()}
					>
						<MoreHorizontal className="h-3.5 w-3.5" />
						<span className="sr-only">Actions</span>
					</Button>
				</DropdownMenuTrigger>
				<DropdownMenuContent align="end">
					<DropdownMenuItem
						className="text-red-500 cursor-pointer"
						onClick={(e) => {
							e.stopPropagation();
							onDelete?.();
						}}
					>
						<Trash className="mr-2 h-4 w-4" />
						<span>Delete</span>
					</DropdownMenuItem>
				</DropdownMenuContent>
			</DropdownMenu>
		</div>
	);
});
