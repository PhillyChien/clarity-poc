package com.aifinancial.clarity.poc.service;

import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.Todo;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.FolderRepository;
import com.aifinancial.clarity.poc.repository.TodoRepository;
import com.aifinancial.clarity.poc.service.impl.ModeratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class ModeratorServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private ModeratorServiceImpl moderatorService;

    private User normalUser;
    private User moderatorUser;
    private Folder folder1;
    private Folder folder2;
    private Todo todo1;
    private Todo todo2;
    private Todo todo3;
    private List<Folder> allFolders;
    private List<Todo> allTodos;

    @BeforeEach
    void setUp() {
        // 創建測試用戶
        normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("normal_user");
        normalUser.setEmail("normal@example.com");
        normalUser.setPassword("password");
        normalUser.setRole(Role.NORMAL);

        moderatorUser = new User();
        moderatorUser.setId(2L);
        moderatorUser.setUsername("moderator_user");
        moderatorUser.setEmail("moderator@example.com");
        moderatorUser.setPassword("password");
        moderatorUser.setRole(Role.MODERATOR);

        // 創建測試文件夾
        folder1 = new Folder();
        folder1.setId(1L);
        folder1.setName("Test Folder 1");
        folder1.setDescription("Test Description 1");
        folder1.setOwner(normalUser);
        folder1.setCreatedAt(OffsetDateTime.now());
        folder1.setUpdatedAt(OffsetDateTime.now());

        folder2 = new Folder();
        folder2.setId(2L);
        folder2.setName("Test Folder 2");
        folder2.setDescription("Test Description 2");
        folder2.setOwner(moderatorUser);
        folder2.setCreatedAt(OffsetDateTime.now());
        folder2.setUpdatedAt(OffsetDateTime.now());

        // 創建測試待辦事項
        todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("Test Todo 1");
        todo1.setDescription("Todo Description 1");
        todo1.setCompleted(false);
        todo1.setDisabled(false);
        todo1.setOwner(normalUser);
        todo1.setFolder(folder1);
        todo1.setCreatedAt(OffsetDateTime.now());
        todo1.setUpdatedAt(OffsetDateTime.now());

        todo2 = new Todo();
        todo2.setId(2L);
        todo2.setTitle("Test Todo 2");
        todo2.setDescription("Todo Description 2");
        todo2.setCompleted(true);
        todo2.setDisabled(false);
        todo2.setOwner(normalUser);
        todo2.setFolder(folder1);
        todo2.setCreatedAt(OffsetDateTime.now());
        todo2.setUpdatedAt(OffsetDateTime.now());

        todo3 = new Todo();
        todo3.setId(3L);
        todo3.setTitle("Test Todo 3");
        todo3.setDescription("Todo Description 3");
        todo3.setCompleted(false);
        todo3.setDisabled(true); // 已禁用
        todo3.setOwner(moderatorUser);
        todo3.setFolder(folder2);
        todo3.setCreatedAt(OffsetDateTime.now());
        todo3.setUpdatedAt(OffsetDateTime.now());

        // 創建列表
        allFolders = Arrays.asList(folder1, folder2);
        allTodos = Arrays.asList(todo1, todo2, todo3);
    }

    @Test
    void testGetAllFolders() {
        // 模擬存儲庫
        when(folderRepository.findAll()).thenReturn(allFolders);

        // 執行測試
        List<FolderResponse> result = moderatorService.getAllFolders();

        // 驗證結果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(folder1.getId(), result.get(0).getId());
        assertEquals(folder1.getName(), result.get(0).getName());
        assertEquals(folder1.getOwner().getId(), result.get(0).getOwnerId());
        
        // 驗證方法調用
        verify(folderRepository, times(1)).findAll();
    }

    @Test
    void testGetAllTodos() {
        // 模擬存儲庫
        when(todoRepository.findAll()).thenReturn(allTodos);

        // 執行測試
        List<TodoResponse> result = moderatorService.getAllTodos();

        // 驗證結果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(todo1.getId(), result.get(0).getId());
        assertEquals(todo1.getTitle(), result.get(0).getTitle());
        assertEquals(todo1.isCompleted(), result.get(0).isCompleted());
        assertEquals(todo1.isDisabled(), result.get(0).isDisabled());
        assertEquals(todo1.getOwner().getId(), result.get(0).getOwnerId());
        assertEquals(todo1.getFolder().getId(), result.get(0).getFolderId());
        
        // 驗證方法調用
        verify(todoRepository, times(1)).findAll();
    }

    @Test
    void testToggleTodoDisabledStatusToDisable() {
        // 初始狀態為未禁用
        todo1.setDisabled(false);
        
        // 模擬存儲庫
        when(todoRepository.findById(todo1.getId())).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo1);

        // 執行測試
        MessageResponse result = moderatorService.toggleTodoDisabledStatus(todo1.getId());

        // 驗證結果
        assertNotNull(result);
        assertTrue(result.getMessage().contains("disabled"));
        assertTrue(todo1.isDisabled()); // 現在應該被禁用
        
        // 驗證方法調用
        verify(todoRepository, times(1)).findById(todo1.getId());
        verify(todoRepository, times(1)).save(todo1);
    }

    @Test
    void testToggleTodoDisabledStatusToEnable() {
        // 初始狀態為已禁用
        todo3.setDisabled(true);
        
        // 模擬存儲庫
        when(todoRepository.findById(todo3.getId())).thenReturn(Optional.of(todo3));
        when(todoRepository.save(any(Todo.class))).thenReturn(todo3);

        // 執行測試
        MessageResponse result = moderatorService.toggleTodoDisabledStatus(todo3.getId());

        // 驗證結果
        assertNotNull(result);
        assertTrue(result.getMessage().contains("enabled"));
        assertFalse(todo3.isDisabled()); // 現在應該被啟用
        
        // 驗證方法調用
        verify(todoRepository, times(1)).findById(todo3.getId());
        verify(todoRepository, times(1)).save(todo3);
    }

    @Test
    void testToggleTodoDisabledStatusTodoNotFound() {
        // 模擬存儲庫 - 找不到待辦事項
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        // 執行測試
        MessageResponse result = moderatorService.toggleTodoDisabledStatus(999L);

        // 驗證結果
        assertNotNull(result);
        assertTrue(result.getMessage().contains("Failed to toggle todo status"));
        
        // 驗證方法調用
        verify(todoRepository, times(1)).findById(999L);
        verify(todoRepository, never()).save(any(Todo.class));
    }
} 