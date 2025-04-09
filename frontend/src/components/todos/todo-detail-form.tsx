"use client";

import { Button } from "@/components/ui/button";
import {
	Form,
	FormControl,
	FormField,
	FormItem,
	FormLabel,
	FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { cn } from "@/lib/utils";
import type { Folder, Todo, UpdateTodoRequest } from "@/services/backend/types";
import { zodResolver } from "@hookform/resolvers/zod";
import { Ban, CalendarCheck2, CalendarClock } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Badge } from "../ui/badge";
import { Checkbox } from "../ui/checkbox";

const todoSchema = z.object({
	title: z.string().min(1, { message: "Title is required" }),
	description: z.string().optional(),
	completed: z.boolean().optional(),
	folderId: z.number().nullable().optional(),
});

interface TodoDetailFormProps {
	todo: Todo;
	onUpdateTodo: (data: UpdateTodoRequest) => void;
	folders: Folder[];
	mode?: "read" | "edit";
}

export function TodoDetailForm({
	todo,
	onUpdateTodo,
	folders = [],
	mode = "read",
}: TodoDetailFormProps) {
	const isEditMode = mode === "edit";

	const form = useForm<z.infer<typeof todoSchema>>({
		resolver: zodResolver(todoSchema),
		defaultValues: {
			title: todo.title,
			description: todo.description || "",
			completed: todo.completed,
			folderId: todo.folderId,
		},
	});

	const onSubmit = (data: z.infer<typeof todoSchema>) => {
		onUpdateTodo({
			title: data.title,
			description: data.description,
			completed: data.completed,
			folderId: data.folderId ?? undefined,
		});
	};

	// Get folder name by ID
	const getFolderName = (folderId: number | null) => {
		if (folderId === null) return "(-)";
		const folder = folders.find((f) => f.id === folderId);
		return folder ? folder.name : "(-)";
	};

	// Format date for display
	const formatDate = (dateString: string) => {
		const date = new Date(dateString);
		return new Intl.DateTimeFormat("en-US", {
			year: "numeric",
			month: "short",
			day: "numeric",
			hour: "2-digit",
			minute: "2-digit",
		}).format(date);
	};

	// Read-only view of the todo
	if (!isEditMode) {
		return (
			<div className="space-y-6">
				<div className="grid gap-6">
					<div className="space-y-2">
						<h3 className="text-sm font-medium text-gray-500">Title</h3>
						<p>{todo.title}</p>
					</div>

					<div className="space-y-2">
						<h3 className="text-sm font-medium text-gray-500">Description</h3>
						<div className="rounded-md bg-gray-50 p-4 text-sm">
							{todo.description || (
								<span className="text-gray-400">No description provided</span>
							)}
						</div>
					</div>

					<div className="space-y-2">
						<h3 className="text-sm font-medium text-gray-500">Folder</h3>
						<p>{getFolderName(todo.folderId ?? null)}</p>
					</div>

					<div className="space-y-2">
						<h3 className="text-sm font-medium text-gray-500">Status</h3>
						<Badge
							variant="outline"
							className={cn(
								"flex w-fit items-center gap-1",
								todo.completed
									? "bg-green-50 text-green-700 border-green-200"
									: "bg-blue-50 text-blue-700 border-blue-200",
							)}
						>
							{todo.disabled ? (
								<>
									<Ban className="h-3 w-3" />
									<span>Banned</span>
								</>
							) : todo.completed ? (
								<>
									<CalendarCheck2 className="h-3 w-3" />
									<span>Completed</span>
								</>
							) : (
								<>
									<CalendarClock className="h-3 w-3" />
									<span>Active</span>
								</>
							)}
						</Badge>
					</div>

					<div className="space-y-2">
						<h3 className="text-sm font-medium text-gray-500">Dates</h3>
						<div className="grid grid-cols-2 gap-4 text-sm">
							<div>
								<p className="text-gray-500">Created</p>
								<p>{formatDate(todo.createdAt)}</p>
							</div>
							<div>
								<p className="text-gray-500">Last Updated</p>
								<p>{formatDate(todo.updatedAt)}</p>
							</div>
						</div>
					</div>
				</div>
			</div>
		);
	}

	// Edit mode form
	return (
		<Form {...form}>
			<form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
				{/* Title Field */}
				<FormField
					control={form.control}
					name="title"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Title</FormLabel>
							<FormControl>
								<Input {...field} />
							</FormControl>
							<FormMessage />
						</FormItem>
					)}
				/>

				{/* Description Field */}
				<FormField
					control={form.control}
					name="description"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Description</FormLabel>
							<FormControl>
								<Textarea rows={4} {...field} value={field.value || ""} />
							</FormControl>
							<FormMessage />
						</FormItem>
					)}
				/>

				{/* Folder Selection */}
				<FormField
					control={form.control}
					name="folderId"
					render={({ field }) => (
						<FormItem>
							<FormLabel>Folder</FormLabel>
							<Select
								onValueChange={(value) =>
									field.onChange(value === "null" ? null : Number(value))
								}
								defaultValue={
									field.value === null ? "null" : field.value?.toString()
								}
							>
								<FormControl>
									<SelectTrigger>
										<SelectValue placeholder="Select a folder" />
									</SelectTrigger>
								</FormControl>
								<SelectContent>
									<SelectItem value="null">Uncategorized</SelectItem>
									{folders.map((folder) => (
										<SelectItem key={folder.id} value={folder.id.toString()}>
											{folder.name}
										</SelectItem>
									))}
								</SelectContent>
							</Select>
							<FormMessage />
						</FormItem>
					)}
				/>

				{/* Status Toggle */}
				<FormField
					control={form.control}
					name="completed"
					render={({ field }) => (
						<FormItem className="flex flex-row items-start space-x-3 space-y-0 rounded-md">
							<FormControl>
								<Checkbox
									checked={field.value}
									onCheckedChange={field.onChange}
									className="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
								/>
							</FormControl>
							<FormLabel className="cursor-pointer">
								Mark as completed
							</FormLabel>
						</FormItem>
					)}
				/>

				<Button className="mt-4" type="submit">
					Save Changes
				</Button>
			</form>
		</Form>
	);
}
