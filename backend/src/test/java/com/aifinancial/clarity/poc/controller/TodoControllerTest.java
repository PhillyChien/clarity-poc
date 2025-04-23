package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.config.SecurityConfig;
import com.aifinancial.clarity.poc.config.WebConfig;
import com.aifinancial.clarity.poc.dto.request.TodoRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.exception.UnauthorizedException;
import com.aifinancial.clarity.poc.constant.PermissionConstants;
import com.aifinancial.clarity.poc.constant.RoleConstants;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.security.JwtAuthenticationEntryPoint;
import com.aifinancial.clarity.poc.security.JwtAuthenticationFilter;
import com.aifinancial.clarity.poc.security.JwtTokenProvider;
import com.aifinancial.clarity.poc.security.UserDetailsServiceImpl;
import com.aifinancial.clarity.poc.service.TodoService;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class}) // Import necessary configs and components
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TodoService todoService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private TodoResponse todoResponse1;
    private TodoResponse todoResponse2;
    private TodoRequest todoRequest;
    private List<TodoResponse> currentUserTodos;
    private List<TodoResponse> user1Todos;
    private List<TodoResponse> folderTodos;

    @BeforeEach
    void setUp() {
        // Re-initialize objectMapper if necessary (often auto-configured by @WebMvcTest)
        objectMapper.findAndRegisterModules();

        OffsetDateTime now = OffsetDateTime.now();
        todoResponse1 = TodoResponse.builder()
                .id(1L)
                .title("Todo 1")
                .description("Description 1")
                .completed(false)
                .disabled(false)
                .ownerId(1L)
                .ownerUsername("user")
                .folderId(10L)
                .folderName("Folder A")
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();

        todoResponse2 = TodoResponse.builder()
                .id(2L)
                .title("Todo 2")
                .description("Description 2")
                .completed(true)
                .disabled(false)
                .ownerId(1L)
                .ownerUsername("user")
                .folderId(null)
                .folderName(null)
                .createdAt(now.minusHours(2))
                .updatedAt(now.minusHours(1))
                .build();

        todoRequest = TodoRequest.builder()
                .title("New Todo")
                .description("New Desc")
                .completed(false)
                .folderId(10L)
                .build();

        currentUserTodos = Arrays.asList(todoResponse1, todoResponse2);
        user1Todos = Arrays.asList(todoResponse1, todoResponse2); // Assuming user 1 owns these
        folderTodos = Collections.singletonList(todoResponse1); // Assuming only todo1 is in folder 10
    }

    // --- Test GET /todos (Current User) ---
    @Test
    @WithMockUser // Simulates an authenticated user
    void testGetTodos_CurrentUser_Success() throws Exception {
        when(todoService.getCurrentUserTodos()).thenReturn(currentUserTodos);

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Todo 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Todo 2")));

        verify(todoService, times(1)).getCurrentUserTodos();
        verify(todoService, never()).getTodosByUserId(anyLong());
    }

    @Test
    @WithAnonymousUser // Simulates an unauthenticated user
    void testGetTodos_CurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isUnauthorized()); // Expect 401 due to security config

        verify(todoService, never()).getCurrentUserTodos();
    }

    // --- Test GET /todos?userId={userId} ---
    @Test
    @WithMockUser(authorities = {"ROLE_" + RoleConstants.ROLE_MODERATOR}) // Moderator can view others' todos
    void testGetTodos_ByUserId_Success() throws Exception {
        Long targetUserId = 1L;
        when(todoService.getTodosByUserId(targetUserId)).thenReturn(user1Todos);

        mockMvc.perform(get("/todos").param("userId", String.valueOf(targetUserId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ownerId", is(targetUserId.intValue())));

        verify(todoService, times(1)).getTodosByUserId(targetUserId);
        verify(todoService, never()).getCurrentUserTodos();
    }

    @Test
    @WithMockUser // Normal user cannot view others' todos
    void testGetTodos_ByUserId_Forbidden() throws Exception {
        Long targetUserId = 2L; // Trying to access another user's todos
        // Mock the service to throw the expected exception for this scenario
        when(todoService.getTodosByUserId(targetUserId)).thenThrow(new UnauthorizedException("Not authorized"));

        mockMvc.perform(get("/todos").param("userId", String.valueOf(targetUserId)))
                // Expect 403 Forbidden because the service layer should deny access
                // Note: The @PreAuthorize in the controller allows isAuthenticated(),
                // but the service logic enforces moderator/admin role here.
                // The GlobalExceptionHandler will map UnauthorizedException to 403.
               .andExpect(status().isForbidden());

        verify(todoService, times(1)).getTodosByUserId(targetUserId);
    }

    @Test
    @WithAnonymousUser
    void testGetTodos_ByUserId_Unauthorized() throws Exception {
         mockMvc.perform(get("/todos").param("userId", "1"))
                .andExpect(status().isUnauthorized());

        verify(todoService, never()).getTodosByUserId(anyLong());
    }

    // --- Test GET /todos/folder/{folderId} ---
    @Test
    @WithMockUser
    void testGetTodosByFolder_Success() throws Exception {
        Long folderId = 10L;
        when(todoService.getTodosByFolder(folderId)).thenReturn(folderTodos);

        mockMvc.perform(get("/todos/folder/{folderId}", folderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].folderId", is(folderId.intValue())));

        verify(todoService, times(1)).getTodosByFolder(folderId);
    }

    @Test
    @WithMockUser
    void testGetTodosByFolder_NotFound() throws Exception {
        Long nonExistentFolderId = 99L;
        when(todoService.getTodosByFolder(nonExistentFolderId)).thenThrow(new ResourceNotFoundException("Folder not found"));

        mockMvc.perform(get("/todos/folder/{folderId}", nonExistentFolderId))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).getTodosByFolder(nonExistentFolderId);
    }

     @Test
    @WithMockUser
    void testGetTodosByFolder_Forbidden() throws Exception {
        Long otherUserFolderId = 20L;
        when(todoService.getTodosByFolder(otherUserFolderId)).thenThrow(new UnauthorizedException("Not authorized"));

        mockMvc.perform(get("/todos/folder/{folderId}", otherUserFolderId))
               .andExpect(status().isForbidden()); // Service throws Unauthorized -> 403

        verify(todoService, times(1)).getTodosByFolder(otherUserFolderId);
    }

    @Test
    @WithAnonymousUser
    void testGetTodosByFolder_Unauthorized() throws Exception {
        mockMvc.perform(get("/todos/folder/{folderId}", 10L))
                .andExpect(status().isUnauthorized());

        verify(todoService, never()).getTodosByFolder(anyLong());
    }

    // --- Test GET /todos/{id} ---
    @Test
    @WithMockUser
    void testGetTodoById_Success() throws Exception {
        Long todoId = 1L;
        when(todoService.getTodo(todoId)).thenReturn(todoResponse1);

        mockMvc.perform(get("/todos/{id}", todoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(todoId.intValue())))
                .andExpect(jsonPath("$.title", is(todoResponse1.getTitle())));

        verify(todoService, times(1)).getTodo(todoId);
    }

    @Test
    @WithMockUser
    void testGetTodoById_NotFound() throws Exception {
        Long nonExistentTodoId = 99L;
        when(todoService.getTodo(nonExistentTodoId)).thenThrow(new ResourceNotFoundException("Todo not found"));

        mockMvc.perform(get("/todos/{id}", nonExistentTodoId))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).getTodo(nonExistentTodoId);
    }

    @Test
    @WithMockUser
    void testGetTodoById_Forbidden() throws Exception {
        Long otherUserTodoId = 3L; // Assume this belongs to another user
        when(todoService.getTodo(otherUserTodoId)).thenThrow(new UnauthorizedException("Not authorized"));

        mockMvc.perform(get("/todos/{id}", otherUserTodoId))
                .andExpect(status().isForbidden());

        verify(todoService, times(1)).getTodo(otherUserTodoId);
    }


    @Test
    @WithAnonymousUser
    void testGetTodoById_Unauthorized() throws Exception {
        mockMvc.perform(get("/todos/{id}", 1L))
                .andExpect(status().isUnauthorized());

        verify(todoService, never()).getTodo(anyLong());
    }

    // --- Test POST /todos ---
    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OWN_CREATE)
    void testCreateTodo_Success() throws Exception {
        when(todoService.createTodo(any(TodoRequest.class))).thenReturn(todoResponse1); // Return the new todo representation

        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isOk()) // Changed to OK as per controller method return type
                .andExpect(jsonPath("$.id", is(todoResponse1.getId().intValue())))
                .andExpect(jsonPath("$.title", is(todoResponse1.getTitle())));

        verify(todoService, times(1)).createTodo(any(TodoRequest.class));
    }

     @Test
    @WithMockUser // Missing required authority
    void testCreateTodo_Forbidden() throws Exception {
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isForbidden()); // Check @PreAuthorize

        verify(todoService, never()).createTodo(any(TodoRequest.class));
    }

    @Test
    @WithAnonymousUser
    void testCreateTodo_Unauthorized() throws Exception {
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isUnauthorized());

        verify(todoService, never()).createTodo(any(TodoRequest.class));
    }

    // --- Test PUT /todos/{id} ---
    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OWN_EDIT)
    void testUpdateTodo_Success() throws Exception {
        Long todoId = 1L;
        TodoRequest updateRequest = TodoRequest.builder()
                                        .title("Updated Title")
                                        .description("Updated Desc")
                                        .completed(true)
                                        .folderId(null)
                                        .build();
        TodoResponse updatedResponse = TodoResponse.builder()
                                        .id(todoId)
                                        .title(updateRequest.getTitle())
                                        .description(updateRequest.getDescription())
                                        .completed(updateRequest.isCompleted())
                                        .ownerId(1L)
                                        .ownerUsername("user")
                                        .folderId(null)
                                        .createdAt(todoResponse1.getCreatedAt())
                                        .updatedAt(OffsetDateTime.now())
                                        .build();

        when(todoService.updateTodo(eq(todoId), any(TodoRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/todos/{id}", todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(todoId.intValue())))
                .andExpect(jsonPath("$.title", is(updateRequest.getTitle())))
                .andExpect(jsonPath("$.completed", is(true)));

        verify(todoService, times(1)).updateTodo(eq(todoId), any(TodoRequest.class));
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OWN_EDIT)
    void testUpdateTodo_NotFound() throws Exception {
        Long nonExistentTodoId = 99L;
         when(todoService.updateTodo(eq(nonExistentTodoId), any(TodoRequest.class)))
                 .thenThrow(new ResourceNotFoundException("Todo not found"));

        mockMvc.perform(put("/todos/{id}", nonExistentTodoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).updateTodo(eq(nonExistentTodoId), any(TodoRequest.class));
    }

    @Test
    @WithMockUser // Missing authority
    void testUpdateTodo_Forbidden() throws Exception {
        mockMvc.perform(put("/todos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isForbidden());

        verify(todoService, never()).updateTodo(anyLong(), any(TodoRequest.class));
    }

    @Test
    @WithAnonymousUser
    void testUpdateTodo_Unauthorized() throws Exception {
        mockMvc.perform(put("/todos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isUnauthorized());

        verify(todoService, never()).updateTodo(anyLong(), any(TodoRequest.class));
    }

    // --- Test PATCH /todos/{id}/toggle-completed ---
    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OWN_EDIT)
    void testToggleCompleted_Success() throws Exception {
        Long todoId = 1L;
        TodoResponse toggledResponse = todoResponse1; // Assume response shows toggled state
        toggledResponse.setCompleted(!toggledResponse.isCompleted());

        when(todoService.toggleCompleted(todoId)).thenReturn(toggledResponse);

        mockMvc.perform(patch("/todos/{id}/toggle-completed", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(todoId.intValue())))
                .andExpect(jsonPath("$.completed", is(toggledResponse.isCompleted())));

        verify(todoService, times(1)).toggleCompleted(todoId);
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OWN_EDIT)
    void testToggleCompleted_NotFound() throws Exception {
        Long nonExistentTodoId = 99L;
        when(todoService.toggleCompleted(nonExistentTodoId)).thenThrow(new ResourceNotFoundException("Todo not found"));

        mockMvc.perform(patch("/todos/{id}/toggle-completed", nonExistentTodoId))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).toggleCompleted(nonExistentTodoId);
    }

    @Test
    @WithMockUser // Missing authority
    void testToggleCompleted_Forbidden() throws Exception {
        mockMvc.perform(patch("/todos/{id}/toggle-completed", 1L))
                .andExpect(status().isForbidden());

        verify(todoService, never()).toggleCompleted(anyLong());
    }

     @Test
    @WithAnonymousUser
    void testToggleCompleted_Unauthorized() throws Exception {
        mockMvc.perform(patch("/todos/{id}/toggle-completed", 1L))
                .andExpect(status().isUnauthorized());

        verify(todoService, never()).toggleCompleted(anyLong());
    }

    // --- Test DELETE /todos/{id} ---
    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OWN_DELETE)
    void testDeleteTodo_Success() throws Exception {
        Long todoId = 1L;
        doNothing().when(todoService).deleteTodo(todoId);

        mockMvc.perform(delete("/todos/{id}", todoId))
                .andExpect(status().isNoContent()); // Expect 204 No Content

        verify(todoService, times(1)).deleteTodo(todoId);
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OWN_DELETE)
    void testDeleteTodo_NotFound() throws Exception {
        Long nonExistentTodoId = 99L;
        // Mock service to throw exception when todo not found
        doThrow(new ResourceNotFoundException("Todo not found")).when(todoService).deleteTodo(nonExistentTodoId);

        mockMvc.perform(delete("/todos/{id}", nonExistentTodoId))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).deleteTodo(nonExistentTodoId);
    }

    @Test
    @WithMockUser // Missing authority
    void testDeleteTodo_Forbidden() throws Exception {
        mockMvc.perform(delete("/todos/{id}", 1L))
                .andExpect(status().isForbidden());

        verify(todoService, never()).deleteTodo(anyLong());
    }

     @Test
    @WithAnonymousUser
    void testDeleteTodo_Unauthorized() throws Exception {
        mockMvc.perform(delete("/todos/{id}", 1L))
                .andExpect(status().isUnauthorized());

        verify(todoService, never()).deleteTodo(anyLong());
    }

    // --- Test PUT /todos/{id}/toggle-disabled ---
    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OTHERS_BAN)
    void testToggleDisabled_Success() throws Exception {
        Long todoId = 1L;
        MessageResponse response = new MessageResponse("Todo successfully disabled");
        when(todoService.toggleTodoDisabledStatus(todoId)).thenReturn(response);

        mockMvc.perform(put("/todos/{id}/toggle-disabled", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(response.getMessage())));

        verify(todoService, times(1)).toggleTodoDisabledStatus(todoId);
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OTHERS_BAN)
    void testToggleDisabled_NotFound() throws Exception {
         Long nonExistentTodoId = 99L;
        when(todoService.toggleTodoDisabledStatus(nonExistentTodoId))
            .thenThrow(new ResourceNotFoundException("Todo not found"));

        mockMvc.perform(put("/todos/{id}/toggle-disabled", nonExistentTodoId))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).toggleTodoDisabledStatus(nonExistentTodoId);
    }

    @Test
    @WithMockUser(authorities = PermissionConstants.TODOS_OWN_EDIT) // Insufficient authority
    void testToggleDisabled_Forbidden() throws Exception {
        mockMvc.perform(put("/todos/{id}/toggle-disabled", 1L))
                .andExpect(status().isForbidden());

        verify(todoService, never()).toggleTodoDisabledStatus(anyLong());
    }

    @Test
    @WithAnonymousUser
    void testToggleDisabled_Unauthorized() throws Exception {
        mockMvc.perform(put("/todos/{id}/toggle-disabled", 1L))
                .andExpect(status().isUnauthorized());

        verify(todoService, never()).toggleTodoDisabledStatus(anyLong());
    }
}