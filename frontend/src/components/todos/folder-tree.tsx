"use client";

import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import type { Folder as FolderType, Todo } from "@/services/backend/types";
import { useTodoTreeStore } from "@/store";
import { FolderPlus } from "lucide-react";
import { FolderNode } from "./folder-node";

interface FolderTreeProps {
	loading?: boolean;
	ownerId: number;
	folders?: FolderType[];
	todosByFolder?: Record<number, Todo[]>;
	uncategorizedTodos?: Todo[];
	isViewingOtherUser?: boolean;
}

function FolderTreeSkeleton() {
	return (
		<div className="space-y-4 w-60 p-4">
			<Skeleton className="h-5 w-full rounded-md" />
			<div className="space-y-2 ml-6">
				<Skeleton className="h-5 w-full rounded-md" />
				<Skeleton className="h-5 w-full rounded-md" />
				<Skeleton className="h-5 w-full rounded-md" />
				<Skeleton className="h-5 w-full rounded-md" />
			</div>
			<Skeleton className="h-5 w-full rounded-md" />
			<div className="space-y-2 ml-6">
				<Skeleton className="h-5 w-full rounded-md" />
			</div>
			<Skeleton className="h-5 w-full rounded-md" />
			<div className="space-y-2 ml-6">
				<Skeleton className="h-5 w-full rounded-md" />
				<Skeleton className="h-5 w-full rounded-md" />
			</div>
		</div>
	);
}

export function FolderTree({
	loading,
	ownerId,
	folders = [],
	todosByFolder = {},
	uncategorizedTodos = [],
	isViewingOtherUser = false,
}: FolderTreeProps) {
	// Store hooks
	const {
		selectedItemId,
		selectedItemType,
		expandedFolders,
		showUncategorizedTodos,
		toggleUncategorizedTodos,
		openCreateFolderModal,
	} = useTodoTreeStore();

	// Render folder list
	const renderFolderList = () => {
		const folderNodes = folders.map((folder) => {
			const folderTodos = todosByFolder[folder.id] || [];
			const isExpanded = expandedFolders.includes(folder.id);
			const isSelected =
				selectedItemType === "folder" && selectedItemId === folder.id;

			return (
				<div key={folder.id} className="mb-1">
					<FolderNode
						folder={folder}
						todos={folderTodos}
						isExpanded={isExpanded}
						isSelected={isSelected}
					/>
				</div>
			);
		});

		const uncategorizedFolder = {
			id: -1,
			name: "(-)",
			ownerId,
			description: "Uncategorized todos",
			createdAt: new Date().toISOString(),
			updatedAt: new Date().toISOString(),
			ownerUsername: "",
			todoCount: uncategorizedTodos.length,
		};
		const uncategorizedNode = (
			<div key="uncategorized" className="mb-1">
				<FolderNode
					folder={uncategorizedFolder}
					todos={uncategorizedTodos}
					isExpanded={showUncategorizedTodos}
					isSelected={selectedItemType === null && selectedItemId === -1}
					isUncategorized
					onToggleExpand={toggleUncategorizedTodos}
				/>
			</div>
		);

		return [uncategorizedNode, ...folderNodes];
	};

	if (loading) {
		return <FolderTreeSkeleton />;
	}

	return (
		<div className="p-4 border-r h-full w-60">
			<div className="flex items-center w-full justify-between mb-4">
				<h3 className="font-medium">Folders</h3>
				{!isViewingOtherUser && (
					<Button
						variant="ghost"
						size="icon"
						className="h-7 w-7"
						onClick={openCreateFolderModal}
					>
						<FolderPlus className="h-4 w-4" />
						<span className="sr-only">Add new folder</span>
					</Button>
				)}
			</div>
			<div className="space-y-1">{renderFolderList()}</div>
		</div>
	);
}
