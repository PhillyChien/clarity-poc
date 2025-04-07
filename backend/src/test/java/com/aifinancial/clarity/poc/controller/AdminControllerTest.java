package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.exception.GlobalExceptionHandler;
import com.aifinancial.clarity.poc.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private ObjectMapper objectMapper = new ObjectMapper();
    
    private UserResponse userResponse1;
    private UserResponse userResponse2;
    private UserResponse userResponse3;
    private List<UserResponse> userResponses;
    private RoleUpdateRequest roleUpdateRequest;
    private MessageResponse messageResponse;
    private MessageResponse accessDeniedResponse;

    @BeforeEach
    void setUp() {
        // 初始化 MockMvc，添加全局異常處理器
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
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
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(adminService.getAllUsers()).thenReturn(userResponses);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("normal_user")))
                .andExpect(jsonPath("$[0].role", is("NORMAL")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].role", is("MODERATOR")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].role", is("SUPER_ADMIN")));

        verify(adminService, times(1)).getAllUsers();
    }

    @Test
    void testUpdateUserRole() throws Exception {
        when(adminService.updateUserRole(any(RoleUpdateRequest.class))).thenReturn(messageResponse);

        mockMvc.perform(post("/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User role updated successfully to MODERATOR")));

        verify(adminService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }
    
    @Test
    void testUpdateUserRoleWithAccessDenied() throws Exception {
        // 創建測試 SUPER_ADMIN 請求
        RoleUpdateRequest superAdminRequest = new RoleUpdateRequest();
        superAdminRequest.setUserId(1L);
        superAdminRequest.setRole("SUPER_ADMIN");
        
        // 模擬服務返回拒絕消息而不是拋出異常
        when(adminService.updateUserRole(any(RoleUpdateRequest.class))).thenReturn(accessDeniedResponse);

        mockMvc.perform(post("/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(superAdminRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Access denied: Cannot promote users to SUPER_ADMIN role")));

        verify(adminService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }
} 