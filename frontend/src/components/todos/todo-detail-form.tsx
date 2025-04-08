"use client";

import type React from "react";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
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
import type { Folder, Todo, UpdateTodoRequest } from "@/services/backend/types";
import { useMemo, useState } from "react";

interface TodoDetailFormProps {
	todo: Todo;
	onUpdateTodo: (todo: UpdateTodoRequest) => void;
	folders: Folder[];
}

export function TodoDetailForm({
	todo,
	onUpdateTodo,
	folders,
}: TodoDetailFormProps) {
	// derive initial state from props
	const initialFormData = useMemo(
		() => ({
			title: todo.title,
			description: todo.description || "",
			completed: todo.completed,
			folderId: todo.folderId,
		}),
		[todo],
	);

	// use a state to track form changes
	const [formData, setFormData] = useState<UpdateTodoRequest>(initialFormData);

	const handleChange = (
		e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
	) => {
		const { name, value } = e.target;
		setFormData((prev) => ({ ...prev, [name]: value }));
	};

	const handleCheckboxChange = (checked: boolean) => {
		setFormData((prev) => ({ ...prev, completed: checked }));
	};

	const handleFolderChange = (value: string) => {
		const folderId =
			value !== "uncategorized" ? Number.parseInt(value) : undefined;
		setFormData((prev) => ({ ...prev, folderId }));
	};

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		onUpdateTodo(formData);
	};

	// when user clicks "reset" button, restore to initial state
	const handleReset = () => {
		setFormData(initialFormData);
	};

	return (
		<form onSubmit={handleSubmit} className="space-y-6">
			<div className="space-y-6">
				<div>
					<h2 className="text-2xl font-bold">{initialFormData.title}</h2>
					<p className="text-gray-500">Todo</p>
				</div>

				<div className="space-y-4">
					<div className="space-y-2">
						<Label htmlFor="title">Title</Label>
						<Input
							id="title"
							name="title"
							value={formData.title}
							onChange={handleChange}
							required
						/>
					</div>

					<div className="space-y-2">
						<Label htmlFor="description">Description</Label>
						<Textarea
							id="description"
							name="description"
							value={formData.description}
							onChange={handleChange}
							rows={5}
							placeholder="Add detailed information about this task..."
						/>
					</div>

					<div className="space-y-2">
						<Label htmlFor="folder">Folder</Label>
						<Select
							value={formData.folderId?.toString() || "uncategorized"}
							onValueChange={handleFolderChange}
						>
							<SelectTrigger>
								<SelectValue placeholder="Select Folder" />
							</SelectTrigger>
							<SelectContent>
								<SelectItem value="uncategorized">Uncategorized</SelectItem>
								{folders.map((folder) => (
									<SelectItem key={folder.id} value={folder.id.toString()}>
										{folder.name}
									</SelectItem>
								))}
							</SelectContent>
						</Select>
					</div>

					<div className="flex items-center space-x-2 p-4">
						<Checkbox
							id="completed"
							checked={formData.completed}
							onCheckedChange={handleCheckboxChange}
						/>
						<Label htmlFor="completed" className="text-base font-medium">
							Mark as Completed
						</Label>
					</div>
				</div>
			</div>

			<div className="flex space-x-2">
				<Button type="submit">Save Changes</Button>
				<Button type="button" variant="outline" onClick={handleReset}>
					Reset
				</Button>
			</div>
		</form>
	);
}
