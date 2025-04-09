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
import { useTodoTreeStore } from "@/store";
import { useTodoStore } from "@/store/todo.store";
import {
	Ban,
	CheckCircle,
	Circle,
	MoreHorizontal,
	Trash,
	Undo,
} from "lucide-react";
import { memo, useCallback, useState } from "react";

interface TodoNodeProps {
	todo: Todo;
	isSelected?: boolean;
	isReadOnly?: boolean;
	isModerator?: boolean;
}

export const TodoNode = memo(function TodoNode({
	todo,
	isSelected = false,
	isReadOnly = false,
	isModerator = false,
}: TodoNodeProps) {
	const [isLoading, setIsLoading] = useState(false);
	const { openDeleteTodoModal, openBanTodoModal, setSelectedItem } =
		useTodoTreeStore();
	const { toggleTodoCompletion } = useTodoStore();

	const handleSelect = () => {
		console.log("Selecting todo with ID:", todo.id);
		setSelectedItem(todo.id, "todo");
	};

	const handleToggleComplete = useCallback(
		async (e: React.MouseEvent) => {
			e.stopPropagation();
			if (isLoading || isReadOnly) return;

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
		[todo.id, toggleTodoCompletion, isLoading, isReadOnly],
	);

	const handleDelete = useCallback(() => {
		if (isReadOnly) return;

		// 打開確認對話框
		openDeleteTodoModal(todo.id);

		// 用戶確認後，將在 TodoPage 中使用 TodoStore 的 deleteTodo 方法
	}, [isReadOnly, todo.id, openDeleteTodoModal]);

	const handleBan = useCallback(() => {
		if (!isModerator) return;
		openBanTodoModal(todo.id);
	}, [isModerator, todo.id, openBanTodoModal]);

	const isCompleted = todo.completed;
	const isDisabled = todo.disabled;

	return (
		<div
			className={cn(
				"flex items-center w-full rounded-md relative gap-2",
				isSelected ? "bg-accent" : "hover:bg-muted",
			)}
		>
			<Button
				type="button"
				variant="ghost"
				size="icon"
				className={cn(
					"h-6 w-6 p-0 flex-shrink-0 z-10",
					isLoading && "opacity-50 cursor-wait",
					isReadOnly && "opacity-50 pointer-events-none",
				)}
				onClick={handleToggleComplete}
				disabled={isLoading || isReadOnly}
			>
				{isDisabled ? (
					<Ban className="h-4 w-4 text-muted-foreground" />
				) : isCompleted ? (
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
				onClick={handleSelect}
			>
				<span
					className={cn(
						"text-sm truncate",
						isDisabled && "text-muted-foreground",
						(isDisabled || isCompleted) && "line-through",
					)}
				>
					{todo.title}
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
					{isModerator && (
						<DropdownMenuItem
							onClick={(e) => {
								e.stopPropagation();
								handleBan();
							}}
						>
							{isDisabled ? (
								<>
									<Undo className="mr-2 h-4 w-4" />
									<span>Unban</span>
								</>
							) : (
								<>
									<Ban className="mr-2 h-4 w-4" />
									<span>Ban</span>
								</>
							)}
						</DropdownMenuItem>
					)}
					{!isReadOnly && (
						<DropdownMenuItem
							onClick={(e) => {
								e.stopPropagation();
								handleDelete();
							}}
							className="!text-destructive [&_svg]:stroke-destructive"
						>
							<Trash className="mr-2 h-4 w-4" />
							<span>Delete</span>
						</DropdownMenuItem>
					)}
				</DropdownMenuContent>
			</DropdownMenu>
		</div>
	);
});
