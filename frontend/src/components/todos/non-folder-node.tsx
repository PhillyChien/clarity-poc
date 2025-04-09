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
import {
	ChevronDown,
	ChevronRight,
	Folder,
	MoreHorizontal,
	Plus,
} from "lucide-react";
import { TodoNode } from "./todo-node";

interface UncategorizedFolderNodeProps {
	todos: Todo[];
	isExpanded: boolean;
	isSelected: boolean;
	isReadOnly?: boolean;
	isModerator?: boolean;
	onToggleExpand: () => void;
	onAddTodo?: () => void;
}

export function UncategorizedFolderNode({
	todos,
	isExpanded,
	isSelected,
	isReadOnly = false,
	isModerator = false,
	onToggleExpand,
	onAddTodo,
}: UncategorizedFolderNodeProps) {
	const { selectedItemId, selectedItemType, setSelectedItem } =
		useTodoTreeStore();

	const handleSelect = () => {
		setSelectedItem(-1, null);
		onToggleExpand();
	};

	const renderTodos = () => {
		if (!isExpanded) return null;

		return (
			<div className="pl-4 py-1">
				{todos.length === 0 ? (
					<div className="text-sm text-muted-foreground py-1 px-2">
						No uncategorized todos
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

	return (
		<>
			<div className="relative flex items-center mb-1">
				<Button
					variant="ghost"
					size="icon"
					onClick={onToggleExpand}
					className="h-6 w-6 p-0 mr-1"
				>
					{isExpanded ? (
						<ChevronDown className="h-3.5 w-3.5 flex-shrink-0" />
					) : (
						<ChevronRight className="h-3.5 w-3.5 flex-shrink-0" />
					)}
					<span className="sr-only">
						{isExpanded ? "Collapse" : "Expand"} uncategorized todos
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
					<span className="truncate">Uncategorized</span>
				</Button>

				{!isReadOnly && onAddTodo && (
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
							<DropdownMenuItem onClick={onAddTodo}>
								<Plus className="mr-2 h-4 w-4" />
								<span>Add Todo</span>
							</DropdownMenuItem>
						</DropdownMenuContent>
					</DropdownMenu>
				)}
			</div>

			{renderTodos()}
		</>
	);
}
