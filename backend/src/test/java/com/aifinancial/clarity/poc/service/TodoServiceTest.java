package com.aifinancial.clarity.poc.service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import static org.mockito.quality.Strictness.LENIENT;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.Mockito.never;

import com.aifinancial.clarity.poc.dto.request.TodoRequest;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.exception.UnauthorizedException;
import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Permission;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.Todo;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.FolderRepository;
import com.aifinancial.clarity.poc.repository.TodoRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.security.UserDetailsImpl;
import com.aifinancial.clarity.poc.service.impl.TodoServiceImpl;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.constant.RoleConstants;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TodoServiceImpl todoService;

    private User normalUser;
    private User moderatorUser;
    private User adminUser;
    private Folder folder1;
    private Folder folder2;
    private Todo todo1;
    private Todo todo2;
    private Todo todo3;
    private Role normalRole;
    private Role moderatorRole;
    private Role superAdminRole;
    private UserDetailsImpl normalUserDetails;

    private Collection<? extends GrantedAuthority> buildAuthorities(Role role) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        if (role.getPermissions() != null) {
            role.getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName()))
            );
        }
        return authorities;
    }

    @BeforeEach
    void setUp() {
        Permission todoRead = new Permission("todo:read");
        Permission todoWrite = new Permission("todo:write");
        Permission todoDelete = new Permission("todo:delete");
        Permission folderRead = new Permission("folder:read");
        Permission allPermission = new Permission("*");

        normalRole = new Role(1L, RoleConstants.ROLE_NORMAL, new HashSet<>());
        moderatorRole = new Role(2L, RoleConstants.ROLE_MODERATOR, new HashSet<>());
        superAdminRole = new Role(3L, RoleConstants.ROLE_SUPER_ADMIN, new HashSet<>());

        normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("normal_user");
        normalUser.setEmail("normal@example.com");
        normalUser.setPassword("password");
        normalUser.setRole(normalRole);

        moderatorUser = new User();
        moderatorUser.setId(2L);
        moderatorUser.setUsername("moderator_user");
        moderatorUser.setEmail("moderator@example.com");
        moderatorUser.setPassword("password");
        moderatorUser.setRole(moderatorRole);

        adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("admin_user");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setRole(superAdminRole);

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
        todo3.setDisabled(false);
        todo3.setOwner(moderatorUser);
        todo3.setFolder(folder2);
        todo3.setCreatedAt(OffsetDateTime.now());
        todo3.setUpdatedAt(OffsetDateTime.now());

        folder1.setTodos(Arrays.asList(todo1, todo2));
        folder2.setTodos(Collections.singletonList(todo3));

        Collection<? extends GrantedAuthority> normalAuthorities = buildAuthorities(normalRole);

        normalUserDetails = new UserDetailsImpl(
                normalUser.getId(),
                normalUser.getUsername(),
                normalUser.getEmail(),
                normalUser.getPassword(),
                normalAuthorities
        );


        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(userRepository.findById(moderatorUser.getId())).thenReturn(Optional.of(moderatorUser));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(folderRepository.findById(folder1.getId())).thenReturn(Optional.of(folder1));
        when(folderRepository.findById(folder2.getId())).thenReturn(Optional.of(folder2));
        when(todoRepository.findById(todo1.getId())).thenReturn(Optional.of(todo1));
        when(todoRepository.findById(todo2.getId())).thenReturn(Optional.of(todo2));
        when(todoRepository.findById(todo3.getId())).thenReturn(Optional.of(todo3));
        when(todoRepository.findByOwnerOrderByCreatedAtDesc(normalUser)).thenReturn(Arrays.asList(todo1, todo2));
        when(todoRepository.findByFolderOrderByCreatedAtDesc(folder1)).thenReturn(Arrays.asList(todo1, todo2));
    }

    @Test
    void testGetCurrentUserTodos() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);

        List<TodoResponse> result = todoService.getCurrentUserTodos();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(todo1.getId(), result.get(0).getId());
        assertEquals(todo1.getTitle(), result.get(0).getTitle());
        assertEquals(todo1.getDescription(), result.get(0).getDescription());
        assertEquals(todo1.isCompleted(), result.get(0).isCompleted());
        assertEquals(normalUser.getId(), result.get(0).getOwnerId());
        assertEquals(normalUser.getUsername(), result.get(0).getOwnerUsername());
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(todoRepository, times(1)).findByOwnerOrderByCreatedAtDesc(normalUser);
    }

    @Test
    void testGetTodosByFolder() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);

        List<TodoResponse> result = todoService.getTodosByFolder(folder1.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(folder1.getId(), result.get(0).getFolderId());
        assertEquals(folder1.getName(), result.get(0).getFolderName());
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, times(1)).findById(folder1.getId());
        verify(todoRepository, times(1)).findByFolderOrderByCreatedAtDesc(folder1);
    }

    @Test
    void testGetTodosByFolderUnauthorized() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);

        assertThrows(UnauthorizedException.class, () -> todoService.getTodosByFolder(folder2.getId()));
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, times(1)).findById(folder2.getId());
        verify(todoRepository, never()).findByFolderOrderByCreatedAtDesc(any(Folder.class));
    }

    @Test
    void testCreateTodo() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        TodoRequest request = new TodoRequest();
        request.setTitle("New Todo");
        request.setDescription("New Description");
        request.setCompleted(false);
        request.setFolderId(folder1.getId());

        Todo newTodo = new Todo();
        newTodo.setId(4L);
        newTodo.setTitle(request.getTitle());
        newTodo.setDescription(request.getDescription());
        newTodo.setCompleted(request.isCompleted());
        newTodo.setDisabled(false);
        newTodo.setOwner(normalUser);
        newTodo.setFolder(folder1);
        newTodo.setCreatedAt(OffsetDateTime.now());
        newTodo.setUpdatedAt(OffsetDateTime.now());

        when(todoRepository.save(any(Todo.class))).thenReturn(newTodo);

        TodoResponse result = todoService.createTodo(request);

        assertNotNull(result);
        assertEquals(newTodo.getId(), result.getId());
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.isCompleted(), result.isCompleted());
        assertEquals(normalUser.getId(), result.getOwnerId());
        assertEquals(folder1.getId(), result.getFolderId());
        
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, times(1)).findById(folder1.getId());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void testCreateTodoUnauthorizedFolder() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        TodoRequest request = new TodoRequest();
        request.setTitle("Unauthorized Todo");
        request.setDescription("This should fail");
        request.setFolderId(folder2.getId());

        assertThrows(UnauthorizedException.class, () -> {
            todoService.createTodo(request);
        });

        verify(folderRepository, times(1)).findById(folder2.getId());
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void testUpdateTodo() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        TodoRequest request = new TodoRequest();
        request.setTitle("Updated Todo 1");
        request.setDescription("Updated Desc 1");
        request.setCompleted(true);
        request.setFolderId(null);

        Todo updatedTodo = new Todo();
        updatedTodo.setId(todo1.getId());
        updatedTodo.setTitle(request.getTitle());
        updatedTodo.setDescription(request.getDescription());
        updatedTodo.setCompleted(request.isCompleted());
        updatedTodo.setOwner(normalUser);
        updatedTodo.setFolder(null);
        updatedTodo.setCreatedAt(todo1.getCreatedAt());
        updatedTodo.setUpdatedAt(OffsetDateTime.now());

        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

        TodoResponse result = todoService.updateTodo(todo1.getId(), request);

        assertNotNull(result);
        assertEquals(updatedTodo.getId(), result.getId());
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(request.isCompleted(), result.isCompleted());
        assertEquals(null, result.getFolderId());
        
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(todoRepository, times(1)).findById(todo1.getId());
        verify(folderRepository, never()).findById(any());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void testUpdateTodoUnauthorized() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        TodoRequest request = new TodoRequest();
        request.setTitle("Updated Unauthorized Todo");

        assertThrows(UnauthorizedException.class, () -> {
            todoService.updateTodo(todo3.getId(), request);
        });
        
        verify(todoRepository, times(1)).findById(todo3.getId());
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void testToggleCompleted() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        boolean initialCompleted = todo1.isCompleted();
        
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo savedTodo = invocation.getArgument(0);
            savedTodo.setCompleted(!initialCompleted); 
            return savedTodo; 
        });

        TodoResponse result = todoService.toggleCompleted(todo1.getId());

        assertNotNull(result);
        assertEquals(todo1.getId(), result.getId());
        assertEquals(!initialCompleted, result.isCompleted());
        
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(todoRepository, times(1)).findById(todo1.getId());
        verify(todoRepository, times(1)).save(todo1);
    }

    @Test
    void testToggleCompletedUnauthorized() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        assertThrows(UnauthorizedException.class, () -> {
            todoService.toggleCompleted(todo3.getId());
        });
        
        verify(todoRepository, times(1)).findById(todo3.getId());
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void testDeleteTodo() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        todoService.deleteTodo(todo1.getId());

        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(todoRepository, times(1)).findById(todo1.getId());
        verify(todoRepository, times(1)).delete(todo1);
    }

    @Test
    void testDeleteTodoUnauthorized() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        assertThrows(UnauthorizedException.class, () -> {
            todoService.deleteTodo(todo3.getId());
        });

        verify(todoRepository, times(1)).findById(todo3.getId());
        verify(todoRepository, never()).delete(any(Todo.class));
    }
} 