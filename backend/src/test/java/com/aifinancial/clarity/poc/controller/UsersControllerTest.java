package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.config.SecurityConfig;
import com.aifinancial.clarity.poc.config.WebConfig;
import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.exception.BadRequestException;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.constant.PermissionConstants;
import com.aifinancial.clarity.poc.constant.RoleConstants;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.security.*; // Import security components
import com.aifinancial.clarity.poc.service.AdminService; // Import the correct service
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UsersController.class) // Target the specific controller
// Import SecurityConfig to apply method security (@PreAuthorize) and other beans needed
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class})
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService; // Mock security dependencies needed by SecurityConfig/JwtAuthFilter

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider; // Mock security dependencies needed by SecurityConfig/JwtAuthFilter

    private UserResponse userResponse1;
    private UserResponse userResponse2;
    private UserResponse userResponse3;
    private List<UserResponse> userResponses;
    private RoleUpdateRequest roleUpdateRequest;
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper if needed (often handled by @WebMvcTest)
        objectMapper.findAndRegisterModules();

        // Create test user response objects
        userResponse1 = UserResponse.builder()
                .id(1L)
                .username("normal_user")
                .email("normal@example.com")
                .role(RoleConstants.ROLE_NORMAL) // Use RoleConstants
                .createdAt(OffsetDateTime.now().minusDays(2))
                .updatedAt(OffsetDateTime.now().minusDays(1))
                .build();

        userResponse2 = UserResponse.builder()
                .id(2L)
                .username("moderator_user")
                .email("moderator@example.com")
                .role(RoleConstants.ROLE_MODERATOR) // Use RoleConstants
                .createdAt(OffsetDateTime.now().minusHours(10))
                .updatedAt(OffsetDateTime.now().minusHours(5))
                .build();

        userResponse3 = UserResponse.builder()
                .id(3L)
                .username("admin_user")
                .email("admin@example.com")
                .role(RoleConstants.ROLE_SUPER_ADMIN) // Use RoleConstants
                .createdAt(OffsetDateTime.now().minusMinutes(30))
                .updatedAt(OffsetDateTime.now())
                .build();

        userResponses = Arrays.asList(userResponse1, userResponse2, userResponse3);

        // Create role update request
        roleUpdateRequest = new RoleUpdateRequest();
        roleUpdateRequest.setUserId(1L);
        roleUpdateRequest.setRole(RoleConstants.ROLE_MODERATOR); // Use RoleConstants

        // Create message response for successful update
        messageResponse = new MessageResponse("User role updated successfully to " + RoleConstants.ROLE_MODERATOR);
    }

    // --- Test GET /api/users ---
    @Test
    @WithMockUser(authorities = PermissionConstants.USERS_VIEW) // User needs 'users.view' permission
    void testGetAllUsers_Success() throws Exception {
        when(adminService.getAllUsers()).thenReturn(userResponses);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].role", is(RoleConstants.ROLE_NORMAL)))
                .andExpect(jsonPath("$[1].role", is(RoleConstants.ROLE_MODERATOR)))
                .andExpect(jsonPath("$[2].role", is(RoleConstants.ROLE_SUPER_ADMIN)));

        verify(adminService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser // User without 'users.view' permission
    void testGetAllUsers_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden()); // Expect 403 Forbidden due to @PreAuthorize

        verify(adminService, never()).getAllUsers();
    }

    @Test
    @WithAnonymousUser // Unauthenticated user
    void testGetAllUsers_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized()); // Expect 401 Unauthorized

        verify(adminService, never()).getAllUsers();
    }

    // --- Test PUT /api/users/role ---
    @Test
    @WithMockUser(authorities = PermissionConstants.USERS_MANAGE) // User needs 'users.manage' permission
    void testUpdateUserRole_Success() throws Exception {
        when(adminService.updateUserRole(any(RoleUpdateRequest.class))).thenReturn(messageResponse);

        mockMvc.perform(put("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(messageResponse.getMessage())));

        verify(adminService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.USERS_VIEW) // User with insufficient permission
    void testUpdateUserRole_Forbidden_InsufficientPermission() throws Exception {
        mockMvc.perform(put("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleUpdateRequest)))
                .andExpect(status().isForbidden());

        verify(adminService, never()).updateUserRole(any(RoleUpdateRequest.class));
    }

    @Test
    @WithAnonymousUser // Unauthenticated user
    void testUpdateUserRole_Unauthorized() throws Exception {
         mockMvc.perform(put("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleUpdateRequest)))
                .andExpect(status().isUnauthorized());

        verify(adminService, never()).updateUserRole(any(RoleUpdateRequest.class));
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.USERS_MANAGE)
    void testUpdateUserRole_ServiceThrowsAccessDenied() throws Exception {
        // Simulate service denying the promotion to SUPER_ADMIN
        RoleUpdateRequest promoteToAdminRequest = new RoleUpdateRequest(1L, RoleConstants.ROLE_SUPER_ADMIN);
        when(adminService.updateUserRole(any(RoleUpdateRequest.class)))
            .thenThrow(new AccessDeniedException("Cannot promote users to SUPER_ADMIN role"));

        mockMvc.perform(put("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promoteToAdminRequest)))
                .andExpect(status().isForbidden()) // GlobalExceptionHandler maps AccessDeniedException to 403
                .andExpect(jsonPath("$.error", is("Access Denied")))
                .andExpect(jsonPath("$.message", is("Access denied: Cannot promote users to SUPER_ADMIN role")));

        verify(adminService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.USERS_MANAGE)
    void testUpdateUserRole_ServiceThrowsUserNotFound() throws Exception {
        Long nonExistentUserId = 999L;
        RoleUpdateRequest request = new RoleUpdateRequest(nonExistentUserId, RoleConstants.ROLE_MODERATOR);
        when(adminService.updateUserRole(any(RoleUpdateRequest.class)))
            .thenThrow(new ResourceNotFoundException("User not found with ID: " + nonExistentUserId));

        mockMvc.perform(put("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()) // GlobalExceptionHandler maps ResourceNotFoundException to 404
                .andExpect(jsonPath("$.error", is("Resource Not Found")))
                .andExpect(jsonPath("$.message", is("User not found with ID: " + nonExistentUserId)));

        verify(adminService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }

     @Test
    @WithMockUser(authorities = PermissionConstants.USERS_MANAGE)
    void testUpdateUserRole_ServiceThrowsInvalidRole() throws Exception {
        when(adminService.updateUserRole(any(RoleUpdateRequest.class)))
                .thenThrow(new BadRequestException("Invalid role: " + RoleConstants.ROLE_MODERATOR + "_INVALID"));

        RoleUpdateRequest request = new RoleUpdateRequest(1L, RoleConstants.ROLE_MODERATOR);
        mockMvc.perform(put("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // GlobalExceptionHandler maps BadRequestException to 400
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Invalid role: " + RoleConstants.ROLE_MODERATOR + "_INVALID")));

        verify(adminService, times(1)).updateUserRole(any(RoleUpdateRequest.class));
    }

     @Test
    @WithMockUser(authorities = PermissionConstants.USERS_MANAGE)
    void testUpdateUserRole_RequestBodyValidationFailure_NullUserId() throws Exception {
        RoleUpdateRequest invalidRequest = new RoleUpdateRequest(null, RoleConstants.ROLE_MODERATOR); // userId is null

        mockMvc.perform(put("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()) // Validation fails -> 400
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.message", is("{userId=User ID cannot be null}")));


        verify(adminService, never()).updateUserRole(any(RoleUpdateRequest.class));
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.USERS_MANAGE)
    void testUpdateUserRole_RequestBodyValidationFailure_BlankRole() throws Exception {
        RoleUpdateRequest invalidRequest = new RoleUpdateRequest(1L, " "); // role is blank

        mockMvc.perform(put("/api/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()) // Validation fails -> 400
                 .andExpect(jsonPath("$.error", is("Validation Error")))
                 .andExpect(jsonPath("$.message", is("{role=Role cannot be blank}")));

        verify(adminService, never()).updateUserRole(any(RoleUpdateRequest.class));
    }
}