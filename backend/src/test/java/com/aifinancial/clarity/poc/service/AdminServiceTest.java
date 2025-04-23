package com.aifinancial.clarity.poc.service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import static org.mockito.quality.Strictness.LENIENT;
import org.springframework.security.access.AccessDeniedException;

import com.aifinancial.clarity.poc.constant.RoleConstants;
import com.aifinancial.clarity.poc.converter.UserConverter;
import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.exception.BadRequestException;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.RoleRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.service.impl.AdminServiceImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserConverter userConverter;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User normalUser;
    private User moderatorUser;
    private User superAdminUser;
    private Role normalRoleEntity;
    private Role moderatorRoleEntity;
    private Role superAdminRoleEntity;
    private UserResponse normalUserResponse;
    private UserResponse moderatorUserResponse;
    private UserResponse superAdminUserResponse;

    private List<User> allUsers;
    private List<UserResponse> allUserResponses;

    @BeforeEach
    void setUp() {
        normalRoleEntity = new Role(1L, RoleConstants.ROLE_NORMAL, new HashSet<>());
        moderatorRoleEntity = new Role(2L, RoleConstants.ROLE_MODERATOR, new HashSet<>());
        superAdminRoleEntity = new Role(3L, RoleConstants.ROLE_SUPER_ADMIN, new HashSet<>());

        normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("normal_user");
        normalUser.setEmail("normal@example.com");
        normalUser.setPassword("password");
        normalUser.setRole(normalRoleEntity);
        normalUser.setCreatedAt(OffsetDateTime.now());
        normalUser.setUpdatedAt(OffsetDateTime.now());

        moderatorUser = new User();
        moderatorUser.setId(2L);
        moderatorUser.setUsername("moderator_user");
        moderatorUser.setEmail("moderator@example.com");
        moderatorUser.setPassword("password");
        moderatorUser.setRole(moderatorRoleEntity);
        moderatorUser.setCreatedAt(OffsetDateTime.now());
        moderatorUser.setUpdatedAt(OffsetDateTime.now());

        superAdminUser = new User();
        superAdminUser.setId(3L);
        superAdminUser.setUsername("super_admin_user");
        superAdminUser.setEmail("superadmin@example.com");
        superAdminUser.setPassword("password");
        superAdminUser.setRole(superAdminRoleEntity);
        superAdminUser.setCreatedAt(OffsetDateTime.now());
        superAdminUser.setUpdatedAt(OffsetDateTime.now());

        normalUserResponse = UserResponse.builder()
                .id(1L)
                .username("normal_user")
                .email("normal@example.com")
                .role(normalRoleEntity.getName())
                .createdAt(normalUser.getCreatedAt())
                .updatedAt(normalUser.getUpdatedAt())
                .build();

        moderatorUserResponse = UserResponse.builder()
                .id(2L)
                .username("moderator_user")
                .email("moderator@example.com")
                .role(moderatorRoleEntity.getName())
                .createdAt(moderatorUser.getCreatedAt())
                .updatedAt(moderatorUser.getUpdatedAt())
                .build();

        superAdminUserResponse = UserResponse.builder()
                .id(3L)
                .username("super_admin_user")
                .email("superadmin@example.com")
                .role(superAdminRoleEntity.getName())
                .createdAt(superAdminUser.getCreatedAt())
                .updatedAt(superAdminUser.getUpdatedAt())
                .build();

        allUsers = List.of(normalUser, moderatorUser, superAdminUser);
        allUserResponses = List.of(normalUserResponse, moderatorUserResponse, superAdminUserResponse);

        when(userRepository.findAll()).thenReturn(allUsers);
        when(userRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(moderatorUser));
        when(userRepository.findById(3L)).thenReturn(Optional.of(superAdminUser));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        when(roleRepository.findByName(RoleConstants.ROLE_NORMAL)).thenReturn(Optional.of(normalRoleEntity));
        when(roleRepository.findByName(RoleConstants.ROLE_MODERATOR)).thenReturn(Optional.of(moderatorRoleEntity));
        when(roleRepository.findByName(RoleConstants.ROLE_SUPER_ADMIN)).thenReturn(Optional.of(superAdminRoleEntity));
        when(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());

        when(userConverter.toDto(normalUser)).thenReturn(normalUserResponse);
        when(userConverter.toDto(moderatorUser)).thenReturn(moderatorUserResponse);
        when(userConverter.toDto(superAdminUser)).thenReturn(superAdminUserResponse);
        when(userConverter.toDtoList(allUsers)).thenReturn(allUserResponses);
        when(userConverter.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(userConverter.toDtoList(List.of(normalUser))).thenReturn(List.of(normalUserResponse));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("getAllUsers - Success")
    void testGetAllUsers() {
        List<UserResponse> result = adminService.getAllUsers();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(allUserResponses, result);
        verify(userRepository, times(1)).findAll();
        verify(userConverter, times(1)).toDtoList(allUsers);
    }

    @Test
    @DisplayName("updateUserRole - Success (Normal to Moderator)")
    void testUpdateUserRole_Success() {
        Long userIdToUpdate = normalUser.getId();
        String targetRoleName = RoleConstants.ROLE_MODERATOR;
        RoleUpdateRequest request = new RoleUpdateRequest(userIdToUpdate, targetRoleName);

        MessageResponse response = adminService.updateUserRole(request);

        assertNotNull(response);
        assertTrue(response.getMessage().contains(moderatorRoleEntity.getName()), "Response message should contain the new role name");

        verify(userRepository, times(1)).findById(userIdToUpdate);
        verify(roleRepository, times(1)).findByName(targetRoleName.toUpperCase());
        verify(userRepository, times(1)).save(normalUser);
        assertEquals(moderatorRoleEntity, normalUser.getRole(), "User's Role entity should be updated");
        assertEquals(targetRoleName, normalUser.getRole().getName(), "User's role name should be updated");
        verify(userConverter, never()).toDto(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole - Failure (User Not Found)")
    void testUpdateUserRole_UserNotFound() {
        Long nonExistentUserId = 99L;
        RoleUpdateRequest request = new RoleUpdateRequest(nonExistentUserId, RoleConstants.ROLE_MODERATOR);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminService.updateUserRole(request);
        });
        assertTrue(exception.getMessage().contains("User not found with ID: 99"));

        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole - Failure (Role Not Found)")
    void testUpdateUserRole_RoleNotFound() {
        Long userIdToUpdate = normalUser.getId();
        String invalidRoleName = "INVALID_ROLE";
        RoleUpdateRequest request = new RoleUpdateRequest(userIdToUpdate, invalidRoleName);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            adminService.updateUserRole(request);
        });
        assertTrue(exception.getMessage().contains("Invalid role: " + invalidRoleName));

        verify(userRepository, times(1)).findById(userIdToUpdate);
        verify(roleRepository, times(1)).findByName(invalidRoleName.toUpperCase());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole - Failure (Cannot Promote to SUPER_ADMIN)")
    void testUpdateUserRole_CannotPromoteToSuperAdmin() {
        Long userIdToUpdate = moderatorUser.getId();
        String targetRoleName = RoleConstants.ROLE_SUPER_ADMIN;
        RoleUpdateRequest request = new RoleUpdateRequest(userIdToUpdate, targetRoleName);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            adminService.updateUserRole(request);
        });
        assertEquals("Cannot promote users to SUPER_ADMIN role", exception.getMessage());

        verify(userRepository, times(1)).findById(userIdToUpdate);
        verify(roleRepository, times(1)).findByName(targetRoleName.toUpperCase());
        verify(userRepository, never()).save(any(User.class));
    }
} 