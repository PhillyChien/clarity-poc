"use client";

import { MainLayout } from "@/components/layouts/main-layout";
import { AddTodoModal } from "@/components/todos/add-todo-modal";
import { BanTodoModal } from "@/components/todos/ban-todo-modal";
import { CreateFolderModal } from "@/components/todos/create-folder-modal";
import { DeleteTodoModal } from "@/components/todos/delete-todo-modal";
import { FolderTree } from "@/components/todos/folder-tree";
import { ItemDetailView } from "@/components/todos/item-detail-view";
import { useAuth } from "@/store/auth.store";
import { useModeratorStore, useTodoTreeStore } from "@/store";
import { useFolderStore } from "@/store/folder.store";
import { useTodoStore } from "@/store/todo.store";
import { useCallback, useEffect } from "react";
import { UserListSidebar } from "../moderator/user-list-sidebar";
import { usePermission } from "@/store/permission.store";

export function TodoPage() {
	// Authentication and user state
	const { user } = useAuth();
	const { hasPermission } = usePermission();
	
	// 检查是否有查看用户列表的权限
	const canViewUserList = hasPermission("users.view");
	
	// Moderator store for viewing other users' data
	const { isLoading: isModeratorLoading } = useModeratorStore();

	// Folder store for the current user's folders
	const {
		folders,
		addFolder,
		isLoading: isFolderLoading,
		fetchFoldersByUserId,
		fetchUserFolders,
		resetStore: resetFolderStore,
	} = useFolderStore();

	// Todo store for the current user's todos
	const {
		addTodo,
		deleteTodo,
		disableTodo,
		fetchTodosByUserId,
		fetchUserTodos,
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
		closeAddTodoModal,
		closeDeleteTodoModal,
		closeBanTodoModal,
		closeCreateFolderModal,
		selectedUserId,
		setSelectedUser,
		setSelectedItem,
	} = useTodoTreeStore();

	// Loading state for any store
	const isLoading = isModeratorLoading || isFolderLoading || isTodoLoading;

	// Calculate if viewing other user's content
	const isViewingOtherUserContent = canViewUserList && selectedUserId !== null && user?.id !== selectedUserId;

	const treeOwnerId = isViewingOtherUserContent ? selectedUserId : user?.id || 0;

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

		if (canViewUserList) {
			setSelectedUser(user.id);
			handleUserSelect(user.id);
			return;
		}

		// 使用 fetchUserFolders 和 fetchUserTodos 来加载当前用户的数据
		fetchUserFolders();
		fetchUserTodos();
	}, [
		fetchUserFolders,
		fetchUserTodos,
		handleUserSelect,
		canViewUserList,
		setSelectedUser,
		user,
	]);

	// Handle adding a todo
	const handleAddTodo = useCallback(
		async (title: string, description: string, folderId?: number) => {
			if (isViewingOtherUserContent) return;

			try {
				await addTodo({ title, description, folderId });
				
				// 刷新所有待办事项
				await fetchUserTodos();
				
				closeAddTodoModal();
			} catch (error) {
				console.error("Error adding todo:", error);
			}
		},
		[
			isViewingOtherUserContent,
			addTodo,
			fetchUserTodos,
			closeAddTodoModal,
		],
	);

	// Handle creating a folder
	const handleCreateFolder = useCallback(
		async (name: string, description?: string) => {
			if (isViewingOtherUserContent) return;
			
			await addFolder({ name, description });
			
			// 刷新文件夹列表
			await fetchUserFolders();
			
			closeCreateFolderModal();
		},
		[
			isViewingOtherUserContent,
			addFolder,
			closeCreateFolderModal,
			fetchUserFolders,
		],
	);

	// Handle deleting a todo
	const handleDeleteTodo = useCallback(async () => {
		if (isViewingOtherUserContent || !deleteTodoModal.todoId) return;
		
		await deleteTodo(deleteTodoModal.todoId);
		
		// 删除后刷新所有待办事项
		if (!isViewingOtherUserContent) {
			await fetchUserTodos();
		}
		
		closeDeleteTodoModal();
	}, [
		isViewingOtherUserContent,
		deleteTodo,
		deleteTodoModal.todoId,
		closeDeleteTodoModal,
		fetchUserTodos,
	]);

	// Handle banning a todo
	const handleBanTodo = useCallback(async () => {
		if (!hasPermission("todos.others.ban") || !banTodoModal.todoId) return;

		// 找到待办事项的当前状态
		const allTodos = [
			...Object.values(todosByFolder).flat(),
			...uncategorizedTodos,
		];
		const todo = allTodos.find((t) => t.id === banTodoModal.todoId);

		if (todo) {
			await disableTodo(banTodoModal.todoId);
			
			// 刷新列表数据
			await fetchUserTodos();
			
			closeBanTodoModal();
		}
	}, [
		hasPermission,
		disableTodo,
		banTodoModal.todoId,
		closeBanTodoModal,
		todosByFolder,
		uncategorizedTodos,
		fetchUserTodos,
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
					canViewUserList ? (
						<div className="flex h-full">
							<UserListSidebar
								onUserSelect={handleUserSelect}
								onClearSelection={handleClearUserSelection}
							/>
							<div className="flex-1">
								<FolderTree
									loading={isLoading}
									ownerId={treeOwnerId}
									folders={folders}
									todosByFolder={todosByFolder}
									uncategorizedTodos={uncategorizedTodos}
									isViewingOtherUser={isViewingOtherUserContent}
								/>
							</div>
						</div>
					) : (
						<FolderTree
							loading={isLoading}
							ownerId={treeOwnerId}
							folders={folders}
							todosByFolder={todosByFolder}
							uncategorizedTodos={uncategorizedTodos}
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
