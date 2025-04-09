import { create } from "zustand";

/**
 * UI State Store - Manages UI-related pure frontend state
 * Does not include any state related to API or data models
 */

interface UIState {
	// Sidebar
	sidebarExpanded: boolean;
	mobileSidebarOpen: boolean;

	// Sidebar Operations
	toggleSidebar: () => void;
	openMobileSidebar: () => void;
	closeMobileSidebar: () => void;
}

export const useUIStore = create<UIState>((set) => ({
	// Sidebar
	sidebarExpanded: true,
	mobileSidebarOpen: false,

	// Sidebar Operations
	toggleSidebar: () => {
		set((state) => ({ sidebarExpanded: !state.sidebarExpanded }));
	},

	openMobileSidebar: () => {
		set({ mobileSidebarOpen: true });
	},

	closeMobileSidebar: () => {
		set({ mobileSidebarOpen: false });
	},
}));
