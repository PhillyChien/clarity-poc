package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.config.SecurityConfig;
import com.aifinancial.clarity.poc.config.WebConfig;
import com.aifinancial.clarity.poc.constant.PermissionConstants;
import com.aifinancial.clarity.poc.constant.RoleConstants;
import com.aifinancial.clarity.poc.dto.request.FolderRequest;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.exception.UnauthorizedException;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.security.JwtAuthenticationEntryPoint;
import com.aifinancial.clarity.poc.security.JwtAuthenticationFilter;
import com.aifinancial.clarity.poc.security.JwtTokenProvider;
import com.aifinancial.clarity.poc.security.UserDetailsServiceImpl;
import com.aifinancial.clarity.poc.service.FolderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FolderController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class})
public class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Autowired ObjectMapper

    @MockitoBean
    private FolderService folderService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService; // Mock security dependencies needed by SecurityConfig/JwtAuthFilter

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider; // Mock security dependencies needed by SecurityConfig/JwtAuthFilter

    private FolderResponse folderResponse1;
    private FolderResponse folderResponse2;
    private FolderRequest folderRequest;
    private List<FolderResponse> currentUserFolders;
    private List<FolderResponse> user1Folders;


    @BeforeEach
    void setUp() {
        // Configure ObjectMapper for Java 8 date/time types if not already done by Spring
        objectMapper.findAndRegisterModules();

        OffsetDateTime now = OffsetDateTime.now();
        folderResponse1 = FolderResponse.builder()
                .id(1L)
                .name("Folder 1")
                .description("Desc 1")
                .ownerId(1L)
                .ownerUsername("user") // Default user for @WithMockUser
                .todoCount(5)
                .createdAt(now.minusDays(2))
                .updatedAt(now.minusDays(1))
                .build();

        folderResponse2 = FolderResponse.builder()
                .id(2L)
                .name("Folder 2")
                .description("Desc 2")
                .ownerId(1L)
                .ownerUsername("user")
                .todoCount(0)
                .createdAt(now.minusHours(3))
                .updatedAt(now.minusHours(1))
                .build();

         folderRequest = FolderRequest.builder()
                .name("New Folder")
                .description("New Desc")
                .build();

        currentUserFolders = Arrays.asList(folderResponse1, folderResponse2);
        user1Folders = Arrays.asList(folderResponse1, folderResponse2); // Assuming user 1 owns these
    }

    // --- Test GET /folders (Current User) ---
    @Test
    @WithMockUser // Authenticated user
    void testGetFolders_CurrentUser_Success() throws Exception {
        when(folderService.getCurrentUserFolders()).thenReturn(currentUserFolders);

        mockMvc.perform(get("/folders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(folderService, times(1)).getCurrentUserFolders();
        verify(folderService, never()).getFoldersByUserId(anyLong());
    }

    @Test
    @WithAnonymousUser // Unauthenticated user
    void testGetFolders_CurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/folders"))
                .andExpect(status().isUnauthorized()); // Security filter chain denies access

        verify(folderService, never()).getCurrentUserFolders();
    }

    // --- Test GET /folders?userId={userId} ---
    @Test
    @WithMockUser(authorities = {"ROLE_" + RoleConstants.ROLE_MODERATOR}) // Moderator/Admin can view others' folders
    void testGetFolders_ByUserId_Success() throws Exception {
        Long targetUserId = 1L;
        when(folderService.getFoldersByUserId(targetUserId)).thenReturn(user1Folders);

        mockMvc.perform(get("/folders").param("userId", String.valueOf(targetUserId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ownerId", is(targetUserId.intValue())));

        verify(folderService, times(1)).getFoldersByUserId(targetUserId);
        verify(folderService, never()).getCurrentUserFolders();
    }

    @Test
    @WithMockUser // Normal user trying to access another user's folders
    void testGetFolders_ByUserId_Forbidden() throws Exception {
        Long targetUserId = 2L;
        // Assume service layer throws UnauthorizedException for this case
        when(folderService.getFoldersByUserId(targetUserId)).thenThrow(new UnauthorizedException("Not authorized"));

        mockMvc.perform(get("/folders").param("userId", String.valueOf(targetUserId)))
               .andExpect(status().isForbidden()); // GlobalExceptionHandler maps UnauthorizedException to 403

        verify(folderService, times(1)).getFoldersByUserId(targetUserId);
    }

    @Test
    @WithAnonymousUser
    void testGetFolders_ByUserId_Unauthorized() throws Exception {
         mockMvc.perform(get("/folders").param("userId", "1"))
                .andExpect(status().isUnauthorized());

        verify(folderService, never()).getFoldersByUserId(anyLong());
    }

    // --- Test GET /folders/{id} ---
    @Test
    @WithMockUser
    void testGetFolderById_Success() throws Exception {
        Long folderId = 1L;
        when(folderService.getFolder(folderId)).thenReturn(folderResponse1);

        mockMvc.perform(get("/folders/{id}", folderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(folderId.intValue())))
                .andExpect(jsonPath("$.name", is(folderResponse1.getName())));

        verify(folderService, times(1)).getFolder(folderId);
    }

    @Test
    @WithMockUser
    void testGetFolderById_NotFound() throws Exception {
        Long nonExistentFolderId = 99L;
        when(folderService.getFolder(nonExistentFolderId)).thenThrow(new ResourceNotFoundException("Folder not found"));

        mockMvc.perform(get("/folders/{id}", nonExistentFolderId))
                .andExpect(status().isNotFound());

        verify(folderService, times(1)).getFolder(nonExistentFolderId);
    }

     @Test
    @WithMockUser
    void testGetFolderById_Forbidden() throws Exception {
        Long otherUserFolderId = 3L;
        when(folderService.getFolder(otherUserFolderId)).thenThrow(new UnauthorizedException("Not authorized"));

        mockMvc.perform(get("/folders/{id}", otherUserFolderId))
               .andExpect(status().isForbidden()); // UnauthorizedException -> 403

        verify(folderService, times(1)).getFolder(otherUserFolderId);
    }

    @Test
    @WithAnonymousUser
    void testGetFolderById_Unauthorized() throws Exception {
        mockMvc.perform(get("/folders/{id}", 1L))
                .andExpect(status().isUnauthorized());

        verify(folderService, never()).getFolder(anyLong());
    }

    // --- Test POST /folders ---
    @Test
    @WithMockUser(authorities = PermissionConstants.FOLDERS_OWN_CREATE)
    void testCreateFolder_Success() throws Exception {
        // Mock service to return the representation of the created folder
        when(folderService.createFolder(any(FolderRequest.class))).thenReturn(folderResponse1);

        mockMvc.perform(post("/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folderRequest)))
                .andExpect(status().isOk()) // Controller returns ResponseEntity.ok()
                .andExpect(jsonPath("$.id", is(folderResponse1.getId().intValue())))
                .andExpect(jsonPath("$.name", is(folderResponse1.getName()))); // Check against the response mock

        verify(folderService, times(1)).createFolder(any(FolderRequest.class));
    }

    @Test
    @WithMockUser // Missing required authority
    void testCreateFolder_Forbidden() throws Exception {
        mockMvc.perform(post("/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folderRequest)))
                .andExpect(status().isForbidden()); // @PreAuthorize check fails

        verify(folderService, never()).createFolder(any(FolderRequest.class));
    }

    @Test
    @WithAnonymousUser
    void testCreateFolder_Unauthorized() throws Exception {
         mockMvc.perform(post("/folders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folderRequest)))
                .andExpect(status().isUnauthorized());

        verify(folderService, never()).createFolder(any(FolderRequest.class));
    }


    // --- Test PUT /folders/{id} ---
    @Test
    @WithMockUser(authorities = PermissionConstants.FOLDERS_OWN_EDIT)
    void testUpdateFolder_Success() throws Exception {
        Long folderId = 1L;
        FolderRequest updateRequest = FolderRequest.builder().name("Updated Name").description("Updated Desc").build();
        FolderResponse updatedResponse = FolderResponse.builder()
                                            .id(folderId)
                                            .name(updateRequest.getName())
                                            .description(updateRequest.getDescription())
                                            .ownerId(1L).ownerUsername("user").todoCount(5)
                                            .createdAt(folderResponse1.getCreatedAt())
                                            .updatedAt(OffsetDateTime.now())
                                            .build();

        when(folderService.updateFolder(eq(folderId), any(FolderRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/folders/{id}", folderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(folderId.intValue())))
                .andExpect(jsonPath("$.name", is(updateRequest.getName())));

        verify(folderService, times(1)).updateFolder(eq(folderId), any(FolderRequest.class));
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.FOLDERS_OWN_EDIT)
    void testUpdateFolder_NotFound() throws Exception {
        Long nonExistentFolderId = 99L;
        when(folderService.updateFolder(eq(nonExistentFolderId), any(FolderRequest.class)))
            .thenThrow(new ResourceNotFoundException("Folder not found"));

        mockMvc.perform(put("/folders/{id}", nonExistentFolderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folderRequest)))
                .andExpect(status().isNotFound());

        verify(folderService, times(1)).updateFolder(eq(nonExistentFolderId), any(FolderRequest.class));
    }

    @Test
    @WithMockUser // Missing authority
    void testUpdateFolder_Forbidden() throws Exception {
        mockMvc.perform(put("/folders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folderRequest)))
                .andExpect(status().isForbidden());

        verify(folderService, never()).updateFolder(anyLong(), any(FolderRequest.class));
    }

    @Test
    @WithAnonymousUser
    void testUpdateFolder_Unauthorized() throws Exception {
        mockMvc.perform(put("/folders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folderRequest)))
                .andExpect(status().isUnauthorized());

        verify(folderService, never()).updateFolder(anyLong(), any(FolderRequest.class));
    }

    // --- Test DELETE /folders/{id} ---
    @Test
    @WithMockUser(authorities = PermissionConstants.FOLDERS_OWN_DELETE)
    void testDeleteFolder_Success() throws Exception {
        Long folderId = 1L;
        doNothing().when(folderService).deleteFolder(folderId); // Mock void method

        mockMvc.perform(delete("/folders/{id}", folderId))
                .andExpect(status().isNoContent()); // Expect 204

        verify(folderService, times(1)).deleteFolder(folderId);
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.FOLDERS_OWN_DELETE)
    void testDeleteFolder_NotFound() throws Exception {
        Long nonExistentFolderId = 99L;
        doThrow(new ResourceNotFoundException("Folder not found")).when(folderService).deleteFolder(nonExistentFolderId);

        mockMvc.perform(delete("/folders/{id}", nonExistentFolderId))
                .andExpect(status().isNotFound());

        verify(folderService, times(1)).deleteFolder(nonExistentFolderId);
    }

    @Test
    @WithMockUser // Missing authority
    void testDeleteFolder_Forbidden() throws Exception {
         mockMvc.perform(delete("/folders/{id}", 1L))
                .andExpect(status().isForbidden());

        verify(folderService, never()).deleteFolder(anyLong());
    }

     @Test
    @WithAnonymousUser
    void testDeleteFolder_Unauthorized() throws Exception {
         mockMvc.perform(delete("/folders/{id}", 1L))
                .andExpect(status().isUnauthorized());

        verify(folderService, never()).deleteFolder(anyLong());
    }
}