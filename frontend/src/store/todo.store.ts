import { create } from 'zustand';
import { CreateTodoRequest, Todo, UpdateTodoRequest } from '../services/backend/types';
import { todoService } from '../services/backend';

interface TodoState {
  todos: Todo[];
  isLoading: boolean;
  error: string | null;
  selectedFolderId: number | null | 'uncategorized';
  
  // 动作
  fetchAllTodos: () => Promise<void>;
  fetchTodosByFolder: (folderId: number) => Promise<void>;
  fetchUncategorizedTodos: () => Promise<void>;
  getTodo: (todoId: number) => Promise<Todo | undefined>;
  addTodo: (todoData: CreateTodoRequest) => Promise<Todo | undefined>;
  updateTodo: (todoId: number, todoData: UpdateTodoRequest) => Promise<Todo | undefined>;
  toggleTodoCompletion: (todoId: number) => Promise<Todo | undefined>;
  deleteTodo: (todoId: number) => Promise<void>;
  setSelectedFolder: (folderId: number | null | 'uncategorized') => void;
  clearError: () => void;
}

export const useTodoStore = create<TodoState>((set) => ({
  todos: [],
  isLoading: false,
  error: null,
  selectedFolderId: null,
  
  // 获取当前用户的所有Todo
  fetchAllTodos: async () => {
    try {
      set({ isLoading: true, error: null });
      const todos = await todoService.getUserTodos();
      set({ todos, isLoading: false, selectedFolderId: null });
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '获取任务失败',
      });
    }
  },
  
  // 获取特定文件夹内的Todo列表
  fetchTodosByFolder: async (folderId: number) => {
    try {
      set({ isLoading: true, error: null });
      const todos = await todoService.getTodosByFolder(folderId);
      set({ todos, isLoading: false, selectedFolderId: folderId });
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '获取文件夹任务失败',
      });
    }
  },
  
  // 获取未分类的Todo（不在任何文件夹中）
  fetchUncategorizedTodos: async () => {
    try {
      set({ isLoading: true, error: null });
      const todos = await todoService.getUserTodos();
      const uncategorizedTodos = todos.filter(todo => !todo.folderId);
      set({ todos: uncategorizedTodos, isLoading: false, selectedFolderId: 'uncategorized' });
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '获取未分类任务失败',
      });
    }
  },
  
  // 获取单个Todo详情
  getTodo: async (todoId: number) => {
    try {
      set({ isLoading: true, error: null });
      const todo = await todoService.getTodo(todoId);
      set({ isLoading: false });
      return todo;
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '获取任务详情失败',
      });
      return undefined;
    }
  },
  
  // 添加新Todo
  addTodo: async (todoData: CreateTodoRequest) => {
    try {
      set({ isLoading: true, error: null });
      const newTodo = await todoService.createTodo(todoData);
      
      // 如果当前查看的是全部任务或者任务所属的文件夹，则添加到列表中
      set((state) => {
        if (
          state.selectedFolderId === null || 
          state.selectedFolderId === newTodo.folderId ||
          (state.selectedFolderId === 'uncategorized' && !newTodo.folderId)
        ) {
          return {
            todos: [...state.todos, newTodo],
            isLoading: false,
          };
        }
        return { isLoading: false };
      });
      
      return newTodo;
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '创建任务失败',
      });
      return undefined;
    }
  },
  
  // 更新Todo
  updateTodo: async (todoId: number, todoData: UpdateTodoRequest) => {
    try {
      set({ isLoading: true, error: null });
      const updatedTodo = await todoService.updateTodo(todoId, todoData);
      
      // 更新状态中的Todo
      set((state) => {
        // 检查更新后的Todo是否应该在当前视图中
        const shouldBeInCurrentView = 
          state.selectedFolderId === null || 
          state.selectedFolderId === updatedTodo.folderId ||
          (state.selectedFolderId === 'uncategorized' && !updatedTodo.folderId);
        
        if (shouldBeInCurrentView) {
          return {
            todos: state.todos.map((todo) => 
              todo.id === todoId ? updatedTodo : todo
            ),
            isLoading: false,
          };
        } else {
          // 如果不应该在当前视图中，则将其移除
          return {
            todos: state.todos.filter((todo) => todo.id !== todoId),
            isLoading: false,
          };
        }
      });
      
      return updatedTodo;
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '更新任务失败',
      });
      return undefined;
    }
  },
  
  // 切换Todo的完成状态
  toggleTodoCompletion: async (todoId: number) => {
    try {
      set({ isLoading: true, error: null });
      const updatedTodo = await todoService.toggleTodoCompletion(todoId);
      
      // 更新状态中的Todo
      set((state) => ({
        todos: state.todos.map((todo) => 
          todo.id === todoId ? updatedTodo : todo
        ),
        isLoading: false,
      }));
      
      return updatedTodo;
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '切换任务状态失败',
      });
      return undefined;
    }
  },
  
  // 删除Todo
  deleteTodo: async (todoId: number) => {
    try {
      set({ isLoading: true, error: null });
      await todoService.deleteTodo(todoId);
      
      // 从状态中删除
      set((state) => ({
        todos: state.todos.filter((todo) => todo.id !== todoId),
        isLoading: false,
      }));
    } catch (error: unknown) {
      set({
        isLoading: false,
        error: error instanceof Error ? error.message : '删除任务失败',
      });
    }
  },
  
  // 设置当前选中的文件夹
  setSelectedFolder: (folderId: number | null | 'uncategorized') => {
    set({ selectedFolderId: folderId });
  },
  
  // 清除错误
  clearError: () => {
    set({ error: null });
  },
})); 