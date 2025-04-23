package com.aifinancial.clarity.poc.service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import static org.mockito.quality.Strictness.LENIENT;

import com.aifinancial.clarity.poc.converter.UserConverter;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.Todo;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.FolderRepository;
import com.aifinancial.clarity.poc.repository.TodoRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.service.impl.ModeratorServiceImpl;
import com.aifinancial.clarity.poc.constant.RoleConstants;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class ModeratorServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private FolderRepository folderRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserConverter userConverter;

    @InjectMocks
    private ModeratorServiceImpl moderatorService;

    private User normalUser;
    private User moderatorUser;
    private Folder folder1;
    private Folder folder2;
    private Todo todo1;
    private Todo todo2;
    private Todo todo3;
    private Role normalRole;
    private Role moderatorRole;

    @BeforeEach
    void setUp() {
        // Create Role entities (simple version for this test)
        normalRole = new Role(1L, RoleConstants.ROLE_NORMAL, new HashSet<>());
        moderatorRole = new Role(2L, RoleConstants.ROLE_MODERATOR, new HashSet<>());
        
        // Create test users with Role entities
        normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("normal_user");
        normalUser.setEmail("normal@example.com");
        normalUser.setPassword("password");
        normalUser.setRole(normalRole); // Use Role entity

        moderatorUser = new User();
        moderatorUser.setId(2L);
        moderatorUser.setUsername("moderator_user");
        moderatorUser.setEmail("moderator@example.com");
        moderatorUser.setPassword("password");
        moderatorUser.setRole(moderatorRole); // Use Role entity

        // Create test folders (ensure owners have roles set)
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

        // Create test todos (ensure owners have roles set)
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
        todo3.setDisabled(true); // Start as disabled
        todo3.setOwner(moderatorUser);
        todo3.setFolder(folder2);
        todo3.setCreatedAt(OffsetDateTime.now());
        todo3.setUpdatedAt(OffsetDateTime.now());
        
        // Mock repository responses (ensure User objects with Roles are returned)
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(userRepository.findById(moderatorUser.getId())).thenReturn(Optional.of(moderatorUser));
        when(todoRepository.findById(todo1.getId())).thenReturn(Optional.of(todo1));
        when(todoRepository.findById(todo3.getId())).thenReturn(Optional.of(todo3));
        when(folderRepository.findByOwnerOrderByCreatedAtDesc(normalUser)).thenReturn(Arrays.asList(folder1));
        when(todoRepository.findByOwnerOrderByCreatedAtDesc(normalUser)).thenReturn(Arrays.asList(todo1, todo2));
    }
    
    @Test
    void testGetAllUsers() {
        // 测试getAllUsers方法，这里简单验证方法调用
        moderatorService.getAllUsers();
        verify(userRepository, times(1)).findAll();
        verify(userConverter, times(1)).toDtoList(any());
    }

    @Test
    void testGetFoldersByUserId() {
        // Mocks already set up in setUp
        // Execute test
        List<FolderResponse> result = moderatorService.getFoldersByUserId(normalUser.getId());

        // Verify result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(folder1.getId(), result.get(0).getId());
        assertEquals(folder1.getName(), result.get(0).getName());
        assertEquals(folder1.getOwner().getId(), result.get(0).getOwnerId());
        
        // Verify method calls
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, times(1)).findByOwnerOrderByCreatedAtDesc(normalUser);
    }

    @Test
    void testGetTodosByUserId() {
        // Mocks already set up in setUp
        // Execute test
        List<TodoResponse> result = moderatorService.getTodosByUserId(normalUser.getId());

        // Verify result
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(todo1.getId(), result.get(0).getId());
        assertEquals(todo1.getTitle(), result.get(0).getTitle());
        
        // Verify method calls
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(todoRepository, times(1)).findByOwnerOrderByCreatedAtDesc(normalUser);
    }

    @Test
    void testToggleTodoDisabledStatusToDisable() {
        // Initial state is not disabled (set in setUp)
        assertFalse(todo1.isDisabled());
        
        // Mocks already set up in setUp
        when(todoRepository.save(any(Todo.class))).thenReturn(todo1); // Mock save

        // Execute test
        MessageResponse result = moderatorService.toggleTodoDisabledStatus(todo1.getId());

        // Verify result
        assertNotNull(result);
        assertTrue(result.getMessage().contains("disabled"));
        assertTrue(todo1.isDisabled()); // Should now be disabled
        
        // Verify method calls
        verify(todoRepository, times(1)).findById(todo1.getId());
        verify(todoRepository, times(1)).save(todo1);
    }

    @Test
    void testToggleTodoDisabledStatusToEnable() {
        // Initial state is disabled (set in setUp)
        assertTrue(todo3.isDisabled());
        
        // Mocks already set up in setUp
        when(todoRepository.save(any(Todo.class))).thenReturn(todo3); // Mock save

        // Execute test
        MessageResponse result = moderatorService.toggleTodoDisabledStatus(todo3.getId());

        // Verify result
        assertNotNull(result);
        assertTrue(result.getMessage().contains("enabled"));
        assertFalse(todo3.isDisabled()); // Should now be enabled
        
        // Verify method calls
        verify(todoRepository, times(1)).findById(todo3.getId());
        verify(todoRepository, times(1)).save(todo3);
    }

    @Test
    void testToggleTodoDisabledStatusTodoNotFound() {
        // Mock repository - todo not found
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute test & verify exception
        assertThrows(ResourceNotFoundException.class, () -> moderatorService.toggleTodoDisabledStatus(999L));
        
        // Verify method calls
        verify(todoRepository, times(1)).findById(999L);
        verify(todoRepository, never()).save(any(Todo.class));
    }
    
    @Test
    void testGetFoldersByUserIdUserNotFound() {
        // Mock repository - user not found
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute test & verify exception
        assertThrows(ResourceNotFoundException.class, () -> moderatorService.getFoldersByUserId(999L));

        // Verify method calls
        verify(userRepository, times(1)).findById(999L);
        verify(folderRepository, never()).findByOwnerOrderByCreatedAtDesc(any(User.class));
    }

    @Test
    void testGetTodosByUserIdUserNotFound() {
        // Mock repository - user not found
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute test & verify exception
        assertThrows(ResourceNotFoundException.class, () -> moderatorService.getTodosByUserId(999L));

        // Verify method calls
        verify(userRepository, times(1)).findById(999L);
        verify(todoRepository, never()).findByOwnerOrderByCreatedAtDesc(any(User.class));
    }
} 