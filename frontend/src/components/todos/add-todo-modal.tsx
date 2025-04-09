"use client";

import type React from "react";

import { Button } from "@/components/ui/button";
import {
	Dialog,
	DialogContent,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useFolderStore } from "@/store/folder.store";
import { useState } from "react";

interface AddTodoModalProps {
	isOpen: boolean;
	onClose: () => void;
	onAddTodo: (title: string, description: string, folderId?: number) => void;
	folderId?: number;
	folderName?: string;
}

export function AddTodoModal({
	isOpen,
	onClose,
	onAddTodo,
	folderId,
	folderName,
}: AddTodoModalProps) {
	const { folders } = useFolderStore();

	const displayFolderName =
		folderName ||
		(folderId ? folders.find((f) => f.id === folderId)?.name : undefined) ||
		"(-)";

	const [title, setTitle] = useState("");
	const [description, setDescription] = useState("");

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		if (title.trim()) {
			onAddTodo(title.trim(), description, folderId);
			handleReset();
		}
	};

	const handleReset = () => {
		setTitle("");
		setDescription("");
	};

	return (
		<Dialog
			open={isOpen}
			onOpenChange={(open) => {
				if (!open) {
					onClose();
					handleReset();
				}
			}}
		>
			<DialogContent className="sm:max-w-[425px]">
				<form onSubmit={handleSubmit}>
					<DialogHeader>
						<DialogTitle>Add New Todo to {displayFolderName}</DialogTitle>
					</DialogHeader>
					<div className="space-y-4 py-4">
						<div className="space-y-2">
							<Label htmlFor="title">Title</Label>
							<Input
								id="title"
								value={title}
								onChange={(e) => setTitle(e.target.value)}
								placeholder="Enter todo title"
								required
							/>
						</div>
						<div className="space-y-2">
							<Label htmlFor="description">Description</Label>
							<Textarea
								id="description"
								value={description}
								onChange={(e) => setDescription(e.target.value)}
								placeholder="Add details..."
								rows={3}
							/>
						</div>
					</div>
					<DialogFooter>
						<Button
							type="button"
							variant="outline"
							onClick={() => {
								onClose();
								handleReset();
							}}
						>
							Cancel
						</Button>
						<Button type="submit">Add</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
