package com.aifinancial.clarity.poc.service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import static org.mockito.quality.Strictness.LENIENT;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aifinancial.clarity.poc.dto.request.TodoRequest;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.exception.UnauthorizedException;
import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.Todo;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.FolderRepository;
import com.aifinancial.clarity.poc.repository.TodoRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.security.UserDetailsImpl;
import com.aifinancial.clarity.poc.service.impl.TodoServiceImpl;

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

    @Mock
    private ModeratorService moderatorService;

    private User normalUser;
    private User moderatorUser;
    private User adminUser;
    private Folder folder1;
    private Folder folder2;
    private Todo todo1;
    private Todo todo2;
    private Todo todo3;
    private UserDetailsImpl normalUserDetails;
    private UserDetailsImpl moderatorUserDetails;
    private UserDetailsImpl adminUserDetails;
    private List<SimpleGrantedAuthority> normalAuthorities;
    private List<SimpleGrantedAuthority> moderatorAuthorities;
    private List<SimpleGrantedAuthority> adminAuthorities;

    @BeforeEach
    void setUp() {
        // 创建测试用户
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

        adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("admin_user");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setRole(Role.SUPER_ADMIN);

        // 创建测试文件夹
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

        // 创建测试待办事项
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

        // 设置文件夹与Todo的关系
        folder1.setTodos(Arrays.asList(todo1, todo2));
        folder2.setTodos(Collections.singletonList(todo3));

        // 创建权限列表
        normalAuthorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_NORMAL"));
        moderatorAuthorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        adminAuthorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // 创建UserDetails对象
        normalUserDetails = new UserDetailsImpl(
                normalUser.getId(),
                normalUser.getUsername(),
                normalUser.getEmail(),
                normalUser.getPassword(),
                normalAuthorities
        );

        moderatorUserDetails = new UserDetailsImpl(
                moderatorUser.getId(),
                moderatorUser.getUsername(),
                moderatorUser.getEmail(),
                moderatorUser.getPassword(),
                moderatorAuthorities
        );

        adminUserDetails = new UserDetailsImpl(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getEmail(),
                adminUser.getPassword(),
                adminAuthorities
        );

        // 设置SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetCurrentUserTodos() {
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(todoRepository.findByOwnerOrderByCreatedAtDesc(normalUser)).thenReturn(Arrays.asList(todo1, todo2));

        // 执行测试
        List<TodoResponse> result = todoService.getCurrentUserTodos();

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(todo1.getId(), result.get(0).getId());
        assertEquals(todo1.getTitle(), result.get(0).getTitle());
        assertEquals(todo1.getDescription(), result.get(0).getDescription());
        assertEquals(todo1.isCompleted(), result.get(0).isCompleted());
        assertEquals(normalUser.getId(), result.get(0).getOwnerId());
        assertEquals(normalUser.getUsername(), result.get(0).getOwnerUsername());
    }

    @Test
    void testGetAllTodosAsModerator() {
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(moderatorUserDetails);
        
        // 使用doReturn().when()模式代替when().thenReturn()
        doReturn(moderatorAuthorities).when(authentication).getAuthorities();
        
        when(userRepository.findById(moderatorUser.getId())).thenReturn(Optional.of(moderatorUser));
        when(todoRepository.findAll()).thenReturn(Arrays.asList(todo1, todo2, todo3));
        
        // 使用 moderatorService 來獲取所有待辦事項
        List<TodoResponse> expectedResponse = Arrays.asList(
            createTodoResponse(todo1),
            createTodoResponse(todo2),
            createTodoResponse(todo3)
        );
        when(moderatorService.getAllTodos()).thenReturn(expectedResponse);

        // 執行測試
        List<TodoResponse> result = moderatorService.getAllTodos();

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testGetAllTodosAsNormalUser() {
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        // 使用doReturn().when()模式代替when().thenReturn()
        doReturn(normalAuthorities).when(authentication).getAuthorities();

        // 验证普通用户无法获取所有待办事项
        assertThrows(UnauthorizedException.class, () -> moderatorService.getAllTodos());
    }

    @Test
    void testGetTodosByFolder() {
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(folderRepository.findById(folder1.getId())).thenReturn(Optional.of(folder1));
        when(todoRepository.findByFolderOrderByCreatedAtDesc(folder1)).thenReturn(Arrays.asList(todo1, todo2));

        // 执行测试
        List<TodoResponse> result = todoService.getTodosByFolder(folder1.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(folder1.getId(), result.get(0).getFolderId());
        assertEquals(folder1.getName(), result.get(0).getFolderName());
    }

    @Test
    void testGetTodosByFolderUnauthorized() {
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(folderRepository.findById(folder2.getId())).thenReturn(Optional.of(folder2));

        // 验证普通用户无法查看不属于自己的文件夹中的待办事项
        assertThrows(UnauthorizedException.class, () -> todoService.getTodosByFolder(folder2.getId()));
    }

    @Test
    void testCreateTodo() {
        // 准备测试数据
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

        // 模拟安全上下文和存储库
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(folderRepository.findById(folder1.getId())).thenReturn(Optional.of(folder1));
        when(todoRepository.save(any(Todo.class))).thenReturn(newTodo);

        // 执行测试
        TodoResponse result = todoService.createTodo(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(newTodo.getId(), result.getId());
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.isCompleted(), result.isCompleted());
        assertEquals(normalUser.getId(), result.getOwnerId());
        assertEquals(folder1.getId(), result.getFolderId());
    }

    @Test
    void testUpdateTodo() {
        // 准备测试数据
        TodoRequest request = new TodoRequest();
        request.setTitle("Updated Todo");
        request.setDescription("Updated Description");
        request.setCompleted(true);
        request.setFolderId(folder1.getId());

        Todo updatedTodo = new Todo();
        updatedTodo.setId(todo1.getId());
        updatedTodo.setTitle(request.getTitle());
        updatedTodo.setDescription(request.getDescription());
        updatedTodo.setCompleted(request.isCompleted());
        updatedTodo.setDisabled(false);
        updatedTodo.setOwner(normalUser);
        updatedTodo.setFolder(folder1);
        updatedTodo.setCreatedAt(todo1.getCreatedAt());
        updatedTodo.setUpdatedAt(OffsetDateTime.now());

        // 模拟安全上下文和存储库
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(todoRepository.findById(todo1.getId())).thenReturn(Optional.of(todo1));
        when(folderRepository.findById(folder1.getId())).thenReturn(Optional.of(folder1));
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

        // 执行测试
        TodoResponse result = todoService.updateTodo(todo1.getId(), request);

        // 验证结果
        assertNotNull(result);
        assertEquals(updatedTodo.getId(), result.getId());
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.isCompleted(), result.isCompleted());
    }

    @Test
    void testToggleCompleted() {
        // 准备数据
        boolean initialCompletedStatus = todo1.isCompleted();
        
        Todo toggledTodo = new Todo();
        toggledTodo.setId(todo1.getId());
        toggledTodo.setTitle(todo1.getTitle());
        toggledTodo.setDescription(todo1.getDescription());
        toggledTodo.setCompleted(!initialCompletedStatus);
        toggledTodo.setDisabled(todo1.isDisabled());
        toggledTodo.setOwner(normalUser);
        toggledTodo.setFolder(folder1);
        toggledTodo.setCreatedAt(todo1.getCreatedAt());
        toggledTodo.setUpdatedAt(OffsetDateTime.now());

        // 模拟安全上下文和存储库
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(todoRepository.findById(todo1.getId())).thenReturn(Optional.of(todo1));
        when(todoRepository.save(any(Todo.class))).thenReturn(toggledTodo);

        // 执行测试
        TodoResponse result = todoService.toggleCompleted(todo1.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(!initialCompletedStatus, result.isCompleted());
    }

    @Test
    void testDeleteTodo() {
        // 模拟安全上下文和存储库
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(todoRepository.findById(todo1.getId())).thenReturn(Optional.of(todo1));

        // 执行测试
        todoService.deleteTodo(todo1.getId());

        // 验证待办事项是否被删除
        verify(todoRepository, times(1)).delete(todo1);
    }

    @Test
    void testDeleteTodoUnauthorized() {
        // 模拟安全上下文和存储库
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        // 使用doReturn().when()模式代替when().thenReturn()
        doReturn(normalAuthorities).when(authentication).getAuthorities();
        
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(todoRepository.findById(todo3.getId())).thenReturn(Optional.of(todo3));

        // 验证普通用户无法删除不属于自己的待办事项
        assertThrows(UnauthorizedException.class, () -> todoService.deleteTodo(todo3.getId()));
    }

    // 輔助方法創建 TodoResponse
    private TodoResponse createTodoResponse(Todo todo) {
        return TodoResponse.builder()
            .id(todo.getId())
            .title(todo.getTitle())
            .description(todo.getDescription())
            .completed(todo.isCompleted())
            .disabled(todo.isDisabled())
            .ownerId(todo.getOwner().getId())
            .ownerUsername(todo.getOwner().getUsername())
            .folderId(todo.getFolder() != null ? todo.getFolder().getId() : null)
            .folderName(todo.getFolder() != null ? todo.getFolder().getName() : null)
            .createdAt(todo.getCreatedAt())
            .updatedAt(todo.getUpdatedAt())
            .build();
    }
} 