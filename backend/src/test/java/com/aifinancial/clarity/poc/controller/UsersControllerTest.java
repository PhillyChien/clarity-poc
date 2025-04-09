package com.aifinancial.clarity.poc.controller;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.exception.BadRequestException;
import com.aifinancial.clarity.poc.exception.GlobalExceptionHandler;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.service.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class UsersControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsersService usersService;

    @InjectMocks
    private UsersController usersController;

    private ObjectMapper objectMapper = new ObjectMapper();
    
    private UserResponse userResponse1;
    private UserResponse userResponse2;
    private UserResponse userResponse3;
    private List<UserResponse> userResponses;
    private RoleUpdateRequest roleUpdateRequest;
    private MessageResponse messageResponse;
    private MessageResponse accessDeniedResponse;
    
    private FolderResponse folderResponse1;
    private FolderResponse folderResponse2;
    private List<FolderResponse> folderResponses;
    
    private TodoResponse todoResponse1;
    private TodoResponse todoResponse2;
    private TodoResponse todoResponse3;
    private List<TodoResponse> todoResponses;
    
    private MessageResponse enabledResponse;
    private MessageResponse disabledResponse;

    @BeforeEach
    void setUp() {
        // 初始化 MockMvc，添加全局異常處理器
        mockMvc = MockMvcBuilders.standaloneSetup(usersController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // 配置 ObjectMapper 以處理 Java 8 日期時間類型
        objectMapper.findAndRegisterModules();
        
        // 創建測試用戶響應對象
        userResponse1 = UserResponse.builder()
                .id(1L)
                .username("normal_user")
                .email("normal@example.com")
                .role("NORMAL")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        userResponse2 = UserResponse.builder()
                .id(2L)
                .username("moderator_user")
                .email("moderator@example.com")
                .role("MODERATOR")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        userResponse3 = UserResponse.builder()
                .id(3L)
                .username("admin_user")
                .email("admin@example.com")
                .role("SUPER_ADMIN")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        userResponses = Arrays.asList(userResponse1, userResponse2, userResponse3);
        
        // 創建角色更新請求
        roleUpdateRequest = new RoleUpdateRequest();
        roleUpdateRequest.setUserId(1L);
        roleUpdateRequest.setRole("MODERATOR");
        
        // 創建消息響應
        messageResponse = new MessageResponse("User role updated successfully to MODERATOR");
        accessDeniedResponse = new MessageResponse("Access denied: Cannot promote users to SUPER_ADMIN role");
        
        // 創建測試文件夾響應對象
        folderResponse1 = FolderResponse.builder()
                .id(1L)
                .name("Test Folder 1")
                .ownerId(1L)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        folderResponse2 = FolderResponse.builder()
                .id(2L)
                .name("Test Folder 2")
                .ownerId(2L)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        folderResponses = Arrays.asList(folderResponse1, folderResponse2);
        
        // 創建測試待辦事項響應對象
        todoResponse1 = TodoResponse.builder()
                .id(1L)
                .title("Test Todo 1")
                .description("Test Description 1")
                .completed(false)
                .disabled(false)
                .ownerId(1L)
                .ownerUsername("normal_user")
                .folderId(1L)
                .folderName("Test Folder 1")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        todoResponse2 = TodoResponse.builder()
                .id(2L)
                .title("Test Todo 2")
                .description("Test Description 2")
                .completed(true)
                .disabled(false)
                .ownerId(1L)
                .ownerUsername("normal_user")
                .folderId(1L)
                .folderName("Test Folder 1")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        todoResponse3 = TodoResponse.builder()
                .id(3L)
                .title("Test Todo 3")
                .description("Test Description 3")
                .completed(false)
                .disabled(true)
                .ownerId(2L)
                .ownerUsername("moderator_user")
                .folderId(2L)
                .folderName("Test Folder 2")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        todoResponses = Arrays.asList(todoResponse1, todoResponse2, todoResponse3);
        
        // 創建消息響應
        enabledResponse = new MessageResponse("Todo successfully enabled");
        disabledResponse = new MessageResponse("Todo successfully disabled");
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(usersService.getAllUsers()).thenReturn(userResponses);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("normal_user")))
                .andExpect(jsonPath("$[0].role", is("NORMAL")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].role", is("MODERATOR")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].role", is("SUPER_ADMIN")));

        verify(usersService, times(1)).getAllUsers();
    }

    @Test
    void testUpdateUserRole() throws Exception {
        when(usersService.updateUserRole(any(RoleUpdateRequest.class))).thenReturn(messageResponse);

        mockMvc.perform(post("/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User role updated successfully to MODERATOR")));

        verify(usersService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }
    
    @Test
    void testUpdateUserRoleWithAccessDenied() throws Exception {
        // 創建測試 SUPER_ADMIN 請求
        RoleUpdateRequest superAdminRequest = new RoleUpdateRequest();
        superAdminRequest.setUserId(1L);
        superAdminRequest.setRole("SUPER_ADMIN");
        
        // 模擬服務拋出 AccessDeniedException
        when(usersService.updateUserRole(any(RoleUpdateRequest.class)))
            .thenThrow(new AccessDeniedException("Cannot promote users to SUPER_ADMIN role"));

        mockMvc.perform(post("/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(superAdminRequest)))
                .andExpect(status().isForbidden());

        verify(usersService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }
    
    @Test
    void testUpdateUserRoleWithUserNotFound() throws Exception {
        // 模擬服務拋出 ResourceNotFoundException
        when(usersService.updateUserRole(any(RoleUpdateRequest.class)))
            .thenThrow(new ResourceNotFoundException("User not found with ID: 999"));

        RoleUpdateRequest nonExistentUserRequest = new RoleUpdateRequest();
        nonExistentUserRequest.setUserId(999L);
        nonExistentUserRequest.setRole("MODERATOR");

        mockMvc.perform(post("/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentUserRequest)))
                .andExpect(status().isNotFound());

        verify(usersService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }
    
    @Test
    void testUpdateUserRoleWithInvalidRole() throws Exception {
        // 模擬服務拋出 BadRequestException
        when(usersService.updateUserRole(any(RoleUpdateRequest.class)))
            .thenThrow(new BadRequestException("Invalid role: INVALID_ROLE"));

        RoleUpdateRequest invalidRoleRequest = new RoleUpdateRequest();
        invalidRoleRequest.setUserId(1L);
        invalidRoleRequest.setRole("INVALID_ROLE");

        mockMvc.perform(post("/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRoleRequest)))
                .andExpect(status().isBadRequest());

        verify(usersService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }
    
    @Test
    void testGetFoldersByUserId() throws Exception {
        when(usersService.getFoldersByUserId(1L)).thenReturn(folderResponses);

        mockMvc.perform(get("/users/1/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Folder 1")))
                .andExpect(jsonPath("$[0].ownerId", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Test Folder 2")))
                .andExpect(jsonPath("$[1].ownerId", is(2)));

        verify(usersService, times(1)).getFoldersByUserId(1L);
    }

    @Test
    void testGetTodosByUserId() throws Exception {
        when(usersService.getTodosByUserId(1L)).thenReturn(todoResponses);

        mockMvc.perform(get("/users/1/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Todo 1")))
                .andExpect(jsonPath("$[0].disabled", is(false)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Test Todo 2")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].title", is("Test Todo 3")))
                .andExpect(jsonPath("$[2].disabled", is(true)));

        verify(usersService, times(1)).getTodosByUserId(1L);
    }

    @Test
    void testToggleTodoStatusToDisable() throws Exception {
        when(usersService.toggleTodoDisabledStatus(1L)).thenReturn(disabledResponse);

        mockMvc.perform(put("/users/todos/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Todo successfully disabled")));

        verify(usersService, times(1)).toggleTodoDisabledStatus(1L);
    }

    @Test
    void testToggleTodoStatusToEnable() throws Exception {
        when(usersService.toggleTodoDisabledStatus(3L)).thenReturn(enabledResponse);

        mockMvc.perform(put("/users/todos/3/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Todo successfully enabled")));

        verify(usersService, times(1)).toggleTodoDisabledStatus(3L);
    }

    @Test
    void testToggleTodoStatusNotFound() throws Exception {
        when(usersService.toggleTodoDisabledStatus(999L)).thenThrow(new ResourceNotFoundException("Todo not found with id: 999"));

        mockMvc.perform(put("/users/todos/999/toggle-status"))
                .andExpect(status().isNotFound());

        verify(usersService, times(1)).toggleTodoDisabledStatus(999L);
    }
} 