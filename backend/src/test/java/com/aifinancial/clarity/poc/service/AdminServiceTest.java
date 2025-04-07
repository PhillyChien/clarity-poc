package com.aifinancial.clarity.poc.service;

import com.aifinancial.clarity.poc.converter.UserConverter;
import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConverter userConverter;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User normalUser;
    private User moderatorUser;
    private User adminUser;
    private UserResponse normalUserResponse;
    private UserResponse moderatorUserResponse;
    private UserResponse adminUserResponse;
    private List<User> allUsers;
    private List<UserResponse> allUserResponses;

    @BeforeEach
    void setUp() {
        // 創建測試用戶
        normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("normal_user");
        normalUser.setEmail("normal@example.com");
        normalUser.setPassword("password");
        normalUser.setRole(Role.NORMAL);
        normalUser.setCreatedAt(OffsetDateTime.now());
        normalUser.setUpdatedAt(OffsetDateTime.now());

        moderatorUser = new User();
        moderatorUser.setId(2L);
        moderatorUser.setUsername("moderator_user");
        moderatorUser.setEmail("moderator@example.com");
        moderatorUser.setPassword("password");
        moderatorUser.setRole(Role.MODERATOR);
        moderatorUser.setCreatedAt(OffsetDateTime.now());
        moderatorUser.setUpdatedAt(OffsetDateTime.now());

        adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("admin_user");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setRole(Role.SUPER_ADMIN);
        adminUser.setCreatedAt(OffsetDateTime.now());
        adminUser.setUpdatedAt(OffsetDateTime.now());

        // 創建測試用戶響應
        normalUserResponse = UserResponse.builder()
                .id(normalUser.getId())
                .username(normalUser.getUsername())
                .email(normalUser.getEmail())
                .role(normalUser.getRole().name())
                .createdAt(normalUser.getCreatedAt())
                .updatedAt(normalUser.getUpdatedAt())
                .build();

        moderatorUserResponse = UserResponse.builder()
                .id(moderatorUser.getId())
                .username(moderatorUser.getUsername())
                .email(moderatorUser.getEmail())
                .role(moderatorUser.getRole().name())
                .createdAt(moderatorUser.getCreatedAt())
                .updatedAt(moderatorUser.getUpdatedAt())
                .build();

        adminUserResponse = UserResponse.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .email(adminUser.getEmail())
                .role(adminUser.getRole().name())
                .createdAt(adminUser.getCreatedAt())
                .updatedAt(adminUser.getUpdatedAt())
                .build();

        // 創建測試用戶列表
        allUsers = Arrays.asList(normalUser, moderatorUser, adminUser);
        allUserResponses = Arrays.asList(normalUserResponse, moderatorUserResponse, adminUserResponse);
    }

    @Test
    void testGetAllUsers() {
        // 模擬存儲庫和轉換器
        when(userRepository.findAll()).thenReturn(allUsers);
        when(userConverter.toDtoList(allUsers)).thenReturn(allUserResponses);

        // 執行測試
        List<UserResponse> result = adminService.getAllUsers();

        // 驗證結果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(normalUserResponse.getId(), result.get(0).getId());
        assertEquals(normalUserResponse.getUsername(), result.get(0).getUsername());
        
        // 驗證方法調用
        verify(userRepository, times(1)).findAll();
        verify(userConverter, times(1)).toDtoList(allUsers);
    }

    @Test
    void testUpdateUserRoleToModerator() {
        // 準備測試數據
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setUserId(normalUser.getId());
        request.setRole("MODERATOR");

        // 模擬存儲庫
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(userRepository.save(any(User.class))).thenReturn(normalUser);

        // 執行測試
        MessageResponse result = adminService.updateUserRole(request);

        // 驗證結果
        assertNotNull(result);
        assertTrue(result.getMessage().contains("successfully"));
        assertEquals(Role.MODERATOR, normalUser.getRole());
        
        // 驗證方法調用
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(userRepository, times(1)).save(normalUser);
    }

    @Test
    void testUpdateUserRoleToNormal() {
        // 準備測試數據
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setUserId(moderatorUser.getId());
        request.setRole("NORMAL");

        // 模擬存儲庫
        when(userRepository.findById(moderatorUser.getId())).thenReturn(Optional.of(moderatorUser));
        when(userRepository.save(any(User.class))).thenReturn(moderatorUser);

        // 執行測試
        MessageResponse result = adminService.updateUserRole(request);

        // 驗證結果
        assertNotNull(result);
        assertTrue(result.getMessage().contains("successfully"));
        assertEquals(Role.NORMAL, moderatorUser.getRole());
        
        // 驗證方法調用
        verify(userRepository, times(1)).findById(moderatorUser.getId());
        verify(userRepository, times(1)).save(moderatorUser);
    }

    @Test
    void testUpdateUserRoleToSuperAdmin() {
        // 準備測試數據
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setUserId(normalUser.getId());
        request.setRole("SUPER_ADMIN");

        // 模擬存儲庫
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));

        // 執行測試並驗證異常
        assertThrows(AccessDeniedException.class, () -> adminService.updateUserRole(request));
        
        // 驗證方法調用
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserRoleInvalidRole() {
        // 準備測試數據
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setUserId(normalUser.getId());
        request.setRole("INVALID_ROLE");

        // 模擬存儲庫
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));

        // 執行測試
        MessageResponse result = adminService.updateUserRole(request);

        // 驗證結果
        assertNotNull(result);
        assertTrue(result.getMessage().contains("Invalid role"));
        
        // 驗證角色未改變
        assertEquals(Role.NORMAL, normalUser.getRole());
        
        // 驗證方法調用
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUserRoleUserNotFound() {
        // 準備測試數據
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setUserId(999L); // 不存在的ID
        request.setRole("MODERATOR");

        // 模擬存儲庫
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 執行測試
        MessageResponse result = adminService.updateUserRole(request);

        // 驗證結果
        assertNotNull(result);
        assertTrue(result.getMessage().contains("Failed to update user role"));
        
        // 驗證方法調用
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }
} 