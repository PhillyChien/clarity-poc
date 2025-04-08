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
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useFolderStore } from "@/store/folder.store";
import { useUIStore } from "@/store/ui.store";
import { useState } from "react";

interface AddTodoModalProps {
	isOpen: boolean;
	onClose: () => void;
	onAddTodo: (title: string, description: string, folderId?: number) => void;
}

export function AddTodoModal({
	isOpen,
	onClose,
	onAddTodo,
}: AddTodoModalProps) {
	const { folders } = useFolderStore();
	const { selectedFolderId } = useUIStore();

	const [title, setTitle] = useState("");
	const [description, setDescription] = useState("");
	const [folderId, setFolderId] = useState<string | undefined>(
		selectedFolderId?.toString(),
	);

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		if (title.trim()) {
			const parsedFolderId =
				folderId && folderId !== "uncategorized"
					? Number.parseInt(folderId)
					: undefined;
			onAddTodo(title.trim(), description, parsedFolderId);
			handleReset();
		}
	};

	const handleReset = () => {
		setTitle("");
		setDescription("");
		setFolderId(selectedFolderId?.toString());
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
						<DialogTitle>Add New Todo</DialogTitle>
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
						<div className="space-y-2">
							<Label htmlFor="folder">Folder</Label>
							<Select value={folderId} onValueChange={setFolderId}>
								<SelectTrigger>
									<SelectValue placeholder="Select a folder" />
								</SelectTrigger>
								<SelectContent>
									{folders.map((folder) => (
										<SelectItem key={folder.id} value={folder.id.toString()}>
											{folder.name}
										</SelectItem>
									))}
								</SelectContent>
							</Select>
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
