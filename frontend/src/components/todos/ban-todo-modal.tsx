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
import { AlertTriangle, CheckCircle } from "lucide-react";

interface BanTodoModalProps {
	isOpen: boolean;
	onClose: () => void;
	onConfirm: () => void;
	todoTitle: string;
	isDisabled?: boolean;
}

/**
 * 待辦事項禁用/啟用的確認對話框
 *
 * 顯示確認對話框，讓用戶確認是否要禁用或啟用某個待辦事項
 *
 * @param isOpen 是否顯示對話框
 * @param onClose 關閉對話框的回調函數
 * @param onConfirm 確認操作的回調函數
 * @param todoTitle 待辦事項的標題
 * @param isDisabled 待辦事項當前是否已禁用
 */
export function BanTodoModal({
	isOpen,
	onClose,
	onConfirm,
	todoTitle,
	isDisabled = false,
}: BanTodoModalProps) {
	const isEnableMode = isDisabled;

	return (
		<Dialog open={isOpen} onOpenChange={onClose}>
			<DialogContent className="sm:max-w-[425px]">
				<DialogHeader>
					<div
						className={`flex items-center gap-2 ${!isEnableMode && "text-destructive"}`}
					>
						{isEnableMode ? (
							<CheckCircle className="h-5 w-5" />
						) : (
							<AlertTriangle className="h-5 w-5" />
						)}
						<DialogTitle>
							{isEnableMode ? "Enable Todo" : "Ban Todo"}
						</DialogTitle>
					</div>
					<DialogDescription className="pt-2">
						{isEnableMode ? (
							<>
								Are you sure you want to unban{" "}
								<span className="font-medium">"{todoTitle}"</span>? This will
								remove the ban from this todo.
							</>
						) : (
							<>
								Are you sure you want to ban{" "}
								<span className="font-medium">"{todoTitle}"</span>? This will
								also disable the todo.
							</>
						)}
					</DialogDescription>
				</DialogHeader>
				<DialogFooter className="gap-2 sm:justify-end">
					<Button variant="outline" onClick={onClose}>
						Cancel
					</Button>
					<Button
						variant={isEnableMode ? "default" : "destructive"}
						onClick={onConfirm}
					>
						{isEnableMode ? "Enable" : "Ban"}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
