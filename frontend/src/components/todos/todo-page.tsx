"use client";

import { MainLayout } from "@/components/layouts/main-layout";
import { AddTodoModal } from "@/components/todos/add-todo-modal";
import { CreateFolderModal } from "@/components/todos/create-folder-modal";
import { DeleteTodoModal } from "@/components/todos/delete-todo-modal";
import { FolderTree } from "@/components/todos/folder-tree";
import { ItemDetailView } from "@/components/todos/item-detail-view";
import { useFolderStore } from "@/store/folder.store";
import { useTodoStore } from "@/store/todo.store";
import { useState } from "react";

export function TodoPage() {
	const [isAddTodoModalOpen, setIsAddTodoModalOpen] = useState(false);
	const [isCreateFolderModalOpen, setIsCreateFolderModalOpen] = useState(false);
	const [isDeleteTodoModalOpen, setIsDeleteTodoModalOpen] = useState(false);
	const [selectedTodoForDelete, setSelectedTodoForDelete] = useState<
		number | null
	>(null);

	const { addFolder } = useFolderStore();
	const { addTodo, deleteTodo } = useTodoStore();

	const handleOpenAddTodoModal = () => {
		setIsAddTodoModalOpen(true);
	};

	const handleAddTodo = async (
		title: string,
		description: string,
		folderId?: number,
	) => {
		await addTodo({ title, description, folderId });
		setIsAddTodoModalOpen(false);
	};

	const handleCreateFolder = async (name: string, description?: string) => {
		await addFolder({ name, description });
		setIsCreateFolderModalOpen(false);
	};

	const handleDeleteTodo = async () => {
		if (selectedTodoForDelete) {
			await deleteTodo(selectedTodoForDelete);
			setSelectedTodoForDelete(null);
			setIsDeleteTodoModalOpen(false);
		}
	};

	return (
		<>
			<MainLayout
				sidebar={
					<FolderTree
						onAddFolderClick={() => setIsCreateFolderModalOpen(true)}
					/>
				}
			>
				<ItemDetailView onAddTodo={handleOpenAddTodoModal} />
			</MainLayout>

			<AddTodoModal
				isOpen={isAddTodoModalOpen}
				onClose={() => setIsAddTodoModalOpen(false)}
				onAddTodo={handleAddTodo}
			/>

			<CreateFolderModal
				isOpen={isCreateFolderModalOpen}
				onClose={() => setIsCreateFolderModalOpen(false)}
				onCreateFolder={handleCreateFolder}
			/>

			<DeleteTodoModal
				isOpen={isDeleteTodoModalOpen}
				onClose={() => setIsDeleteTodoModalOpen(false)}
				onConfirm={handleDeleteTodo}
				todoId={selectedTodoForDelete || undefined}
			/>
		</>
	);
}
