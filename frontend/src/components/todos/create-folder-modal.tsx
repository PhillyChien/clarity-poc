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
import { FolderPlus } from "lucide-react";
import { useState } from "react";

interface CreateFolderModalProps {
	isOpen: boolean;
	onClose: () => void;
	onCreateFolder: (name: string, description?: string) => void;
}

export function CreateFolderModal({
	isOpen,
	onClose,
	onCreateFolder,
}: CreateFolderModalProps) {
	const [folderName, setFolderName] = useState("");
	const [folderDescription, setFolderDescription] = useState("");
	const [error, setError] = useState("");

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();

		if (!folderName.trim()) {
			setError("Folder name cannot be empty");
			return;
		}

		onCreateFolder(folderName.trim(), folderDescription.trim() || undefined);
		handleReset();
		onClose();
	};

	const handleReset = () => {
		setFolderName("");
		setFolderDescription("");
		setError("");
	};

	return (
		<Dialog
			open={isOpen}
			onOpenChange={(open) => {
				if (!open) {
					handleReset();
					onClose();
				}
			}}
		>
			<DialogContent className="sm:max-w-[425px]">
				<form onSubmit={handleSubmit}>
					<DialogHeader>
						<div className="flex items-center gap-2">
							<FolderPlus className="h-5 w-5 text-primary" />
							<DialogTitle>Create New Folder</DialogTitle>
						</div>
					</DialogHeader>
					<div className="space-y-4 py-4">
						<div className="space-y-2">
							<Label htmlFor="folderName">Folder Name</Label>
							<Input
								id="folderName"
								value={folderName}
								onChange={(e) => {
									setFolderName(e.target.value);
									if (e.target.value.trim()) setError("");
								}}
								placeholder="Enter folder name"
								autoFocus
							/>
							{error && <p className="text-sm text-destructive">{error}</p>}
						</div>

						<div className="space-y-2">
							<Label htmlFor="folderDescription">Description (Optional)</Label>
							<Textarea
								id="folderDescription"
								value={folderDescription}
								onChange={(e) => setFolderDescription(e.target.value)}
								placeholder="Enter folder description"
								rows={3}
							/>
						</div>
					</div>
					<DialogFooter>
						<Button type="button" variant="outline" onClick={onClose}>
							Cancel
						</Button>
						<Button type="submit">Create Folder</Button>
					</DialogFooter>
				</form>
			</DialogContent>
		</Dialog>
	);
}
