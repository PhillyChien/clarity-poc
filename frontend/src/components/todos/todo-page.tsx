"use client";

import { MainLayout } from "@/components/layouts/main-layout";
import { AddTodoModal } from "@/components/todos/add-todo-modal";
import { BanTodoModal } from "@/components/todos/ban-todo-modal";
import { CreateFolderModal } from "@/components/todos/create-folder-modal";
import { DeleteTodoModal } from "@/components/todos/delete-todo-modal";
import { FolderTree } from "@/components/todos/folder-tree";
import { ItemDetailView } from "@/components/todos/item-detail-view";
import { useAuth, useModeratorStore, useTodoTreeStore } from "@/store";
import { useFolderStore } from "@/store/folder.store";
import { useTodoStore } from "@/store/todo.store";
import { useCallback, useEffect } from "react";
import { UserListSidebar } from "../moderator/user-list-sidebar";

export function TodoPage() {
	// Authentication and user state
	const { isModerator, user } = useAuth();

	// Moderator store for viewing other users' data
	const { isLoading: isModeratorLoading } = useModeratorStore();

	// Folder store for the current user's folders
	const {
		folders,
		addFolder,
		isLoading: isFolderLoading,
		fetchFoldersByUserId,
		resetStore: resetFolderStore,
	} = useFolderStore();

	// Todo store for the current user's todos
	const {
		addTodo,
		deleteTodo,
		disableTodo,
		fetchTodosByFolder,
		fetchUncategorizedTodos,
		fetchTodosByUserId,
		todosByFolder,
		uncategorizedTodos,
		isLoading: isTodoLoading,
		resetStore: resetTodoStore,
	} = useTodoStore();

	// TodoTree UI store
	const {
		resetState: resetTodoTreeState,
		addTodoModal,
		deleteTodoModal,
		banTodoModal,
		createFolderModal,
		openAddTodoModal,
		closeAddTodoModal,
		closeDeleteTodoModal,
		closeBanTodoModal,
		openCreateFolderModal,
		closeCreateFolderModal,
		selectedUserId,
		setSelectedUser,
		setSelectedItem,
	} = useTodoTreeStore();

	// Loading state for any store
	const isLoading = isModeratorLoading || isFolderLoading || isTodoLoading;

	// Calculate if viewing other user's content
	const isViewingOtherUserContent =
		isModerator() && selectedUserId !== null && user?.id !== selectedUserId;

	// Moderator select user callback function
	const handleUserSelect = useCallback(
		(userId: number) => {
			// Reset store and UI selection
			resetTodoStore();
			resetFolderStore();
			resetTodoTreeState();
			setSelectedItem(null, null);
			setSelectedUser(userId);

			// Get the user's data
			fetchFoldersByUserId(userId);
			fetchTodosByUserId(userId);
		},
		[
			resetTodoStore,
			resetFolderStore,
			resetTodoTreeState,
			setSelectedItem,
			setSelectedUser,
			fetchFoldersByUserId,
			fetchTodosByUserId,
		],
	);

	// Ensure data is initialized when the component loads
	useEffect(() => {
		if (!user) return;

		if (isModerator()) {
			setSelectedUser(user.id);
			handleUserSelect(user.id);
			return;
		}

		fetchFoldersByUserId(user.id);
		fetchUncategorizedTodos();
	}, [
		fetchFoldersByUserId,
		fetchUncategorizedTodos,
		handleUserSelect,
		isModerator,
		setSelectedUser,
		user,
	]);

	// Handle opening the add todo modal - 使用 store 取代本地方法
	const handleOpenAddTodoModal = useCallback(
		(folderId?: number, folderName?: string) => {
			if (isViewingOtherUserContent) return;
			openAddTodoModal(folderId, folderName);
		},
		[isViewingOtherUserContent, openAddTodoModal],
	);

	// Handle adding a todo
	const handleAddTodo = useCallback(
		async (title: string, description: string, folderId?: number) => {
			if (isViewingOtherUserContent) return;

			try {
				const newTodo = await addTodo({ title, description, folderId });

				if (newTodo) {
					// If added to a folder, refresh that folder's todos
					if (newTodo.folderId) {
						await fetchTodosByFolder(newTodo.folderId);
					} else {
						// If uncategorized, refresh those
						await fetchUncategorizedTodos();
					}
				}

				closeAddTodoModal();
			} catch (error) {
				console.error("Error adding todo:", error);
			}
		},
		[
			isViewingOtherUserContent,
			addTodo,
			fetchTodosByFolder,
			fetchUncategorizedTodos,
			closeAddTodoModal,
		],
	);

	// Handle creating a folder
	const handleCreateFolder = useCallback(
		async (name: string, description?: string) => {
			if (isViewingOtherUserContent) return;
			await addFolder({ name, description });
			closeCreateFolderModal();
		},
		[isViewingOtherUserContent, addFolder, closeCreateFolderModal],
	);

	// Handle deleting a todo
	const handleDeleteTodo = useCallback(async () => {
		if (isViewingOtherUserContent || !deleteTodoModal.todoId) return;
		await deleteTodo(deleteTodoModal.todoId);
		closeDeleteTodoModal();
	}, [
		isViewingOtherUserContent,
		deleteTodo,
		deleteTodoModal.todoId,
		closeDeleteTodoModal,
	]);

	// Handle banning a todo
	const handleBanTodo = useCallback(async () => {
		if (!isModerator() || !banTodoModal.todoId) return;

		// 查找待办事项的当前状态
		const allTodos = [
			...Object.values(todosByFolder).flat(),
			...uncategorizedTodos,
		];
		const todo = allTodos.find((t) => t.id === banTodoModal.todoId);

		if (todo) {
			await disableTodo(banTodoModal.todoId);
			closeBanTodoModal();
		}
	}, [
		isModerator,
		disableTodo,
		banTodoModal.todoId,
		closeBanTodoModal,
		todosByFolder,
		uncategorizedTodos,
	]);

	// Handle clear user selection
	const handleClearUserSelection = useCallback(() => {
		// Reset store and UI selection
		resetTodoStore();
		resetFolderStore();
		resetTodoTreeState();
		setSelectedItem(null, null);
	}, [resetTodoStore, resetFolderStore, resetTodoTreeState, setSelectedItem]);

	return (
		<>
			<MainLayout
				sidebar={
					isModerator() ? (
						<div className="flex h-full">
							<UserListSidebar
								onUserSelect={handleUserSelect}
								onClearSelection={handleClearUserSelection}
							/>
							<div className="flex-1">
								<FolderTree
									isModerator
									loading={isLoading}
									onAddFolderClick={openCreateFolderModal}
									folders={folders}
									todosByFolder={todosByFolder}
									uncategorizedTodos={uncategorizedTodos}
									isViewingOtherUser={isViewingOtherUserContent}
									onAddTodoClick={handleOpenAddTodoModal}
								/>
							</div>
						</div>
					) : (
						<FolderTree
							loading={isLoading}
							onAddFolderClick={openCreateFolderModal}
							folders={folders}
							todosByFolder={todosByFolder}
							uncategorizedTodos={uncategorizedTodos}
							onAddTodoClick={handleOpenAddTodoModal}
						/>
					)
				}
			>
				<ItemDetailView isViewingOtherUser={isViewingOtherUserContent} />
			</MainLayout>

			{/* 使用 store 中的狀態控制所有模態框 */}
			<AddTodoModal
				isOpen={addTodoModal.isOpen}
				onClose={closeAddTodoModal}
				onAddTodo={handleAddTodo}
				folderId={addTodoModal.folderId}
				folderName={addTodoModal.folderName}
			/>

			<CreateFolderModal
				isOpen={createFolderModal.isOpen}
				onClose={closeCreateFolderModal}
				onCreateFolder={handleCreateFolder}
			/>

			<DeleteTodoModal
				isOpen={deleteTodoModal.isOpen}
				onClose={closeDeleteTodoModal}
				onConfirm={handleDeleteTodo}
			/>

			{/* Ban Todo Modal */}
			{banTodoModal.todoId &&
				(() => {
					// 查找待办事项的标题和状态
					const allTodos = [
						...Object.values(todosByFolder).flat(),
						...uncategorizedTodos,
					];
					const todo = allTodos.find((t) => t.id === banTodoModal.todoId);

					return (
						<BanTodoModal
							isOpen={banTodoModal.isOpen}
							onClose={closeBanTodoModal}
							onConfirm={handleBanTodo}
							todoTitle={todo?.title || ""}
							isDisabled={todo?.disabled || false}
						/>
					);
				})()}
		</>
	);
}
