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
import { useTodoStore } from "@/store/todo.store";
import { AlertTriangle } from "lucide-react";
import { useEffect, useState } from "react";

interface DeleteTodoModalProps {
	isOpen: boolean;
	onClose: () => void;
	onConfirm: () => void;
	todoId?: number;
}

export function DeleteTodoModal({
	isOpen,
	onClose,
	onConfirm,
	todoId,
}: DeleteTodoModalProps) {
	const { todos } = useTodoStore();
	const [todoTitle, setTodoTitle] = useState("");

	useEffect(() => {
		if (todoId) {
			const todo = todos.find((t) => t.id === todoId);
			if (todo) {
				setTodoTitle(todo.title);
			}
		}
	}, [todoId, todos]);

	const handleDelete = () => {
		onConfirm();
		onClose();
	};

	return (
		<Dialog open={isOpen} onOpenChange={onClose}>
			<DialogContent className="sm:max-w-[425px]">
				<DialogHeader>
					<div className="flex items-center gap-2 text-destructive">
						<AlertTriangle className="h-5 w-5" />
						<DialogTitle>删除待办事项</DialogTitle>
					</div>
					<DialogDescription className="pt-2">
						确定要删除 <span className="font-medium">"{todoTitle}"</span>
						吗？此操作无法撤销。
					</DialogDescription>
				</DialogHeader>
				<DialogFooter className="gap-2 sm:justify-end">
					<Button variant="outline" onClick={onClose}>
						取消
					</Button>
					<Button variant="destructive" onClick={handleDelete}>
						删除
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
