package com.aifinancial.clarity.poc.controller;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import static org.mockito.quality.Strictness.LENIENT;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aifinancial.clarity.poc.dto.request.TodoRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.exception.GlobalExceptionHandler;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class TodoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    private ObjectMapper objectMapper = new ObjectMapper();
    
    private TodoRequest todoRequest;
    private TodoResponse todoResponse1;
    private TodoResponse todoResponse2;
    private List<TodoResponse> todoResponses;
    private MessageResponse enabledResponse;
    private MessageResponse disabledResponse;

    @BeforeEach
    void setUp() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(todoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // 配置ObjectMapper以处理Java 8日期时间类型
        objectMapper.findAndRegisterModules();
        
        // 创建请求对象
        todoRequest = TodoRequest.builder()
                .title("Test Todo")
                .description("Test Description")
                .completed(false)
                .folderId(1L)
                .build();

        // 创建响应对象
        todoResponse1 = TodoResponse.builder()
                .id(1L)
                .title("Test Todo 1")
                .description("Test Description 1")
                .completed(false)
                .disabled(false)
                .ownerId(1L)
                .ownerUsername("user1")
                .folderId(1L)
                .folderName("Folder 1")
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
                .ownerUsername("user1")
                .folderId(1L)
                .folderName("Folder 1")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        todoResponses = Arrays.asList(todoResponse1, todoResponse2);
        
        // 创建消息响应
        enabledResponse = new MessageResponse("Todo successfully enabled");
        disabledResponse = new MessageResponse("Todo successfully disabled");
    }

    @Test
    void testGetCurrentUserTodos() throws Exception {
        when(todoService.getCurrentUserTodos()).thenReturn(todoResponses);

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Todo 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Test Todo 2")));

        verify(todoService, times(1)).getCurrentUserTodos();
    }
    
    @Test
    void testGetTodosByUserId() throws Exception {
        when(todoService.getTodosByUserId(1L)).thenReturn(todoResponses);

        mockMvc.perform(get("/todos?userId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Todo 1")))
                .andExpect(jsonPath("$[0].disabled", is(false)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Test Todo 2")));

        verify(todoService, times(1)).getTodosByUserId(1L);
    }

    @Test
    void testGetTodosByFolder() throws Exception {
        when(todoService.getTodosByFolder(1L)).thenReturn(todoResponses);

        mockMvc.perform(get("/todos/folder/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].folderId", is(1)))
                .andExpect(jsonPath("$[0].folderName", is("Folder 1")));

        verify(todoService, times(1)).getTodosByFolder(1L);
    }

    @Test
    void testGetTodo() throws Exception {
        when(todoService.getTodo(1L)).thenReturn(todoResponse1);

        mockMvc.perform(get("/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Todo 1")));

        verify(todoService, times(1)).getTodo(1L);
    }

    @Test
    void testCreateTodo() throws Exception {
        when(todoService.createTodo(org.mockito.ArgumentMatchers.any(TodoRequest.class))).thenReturn(todoResponse1);

        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Todo 1")));

        verify(todoService, times(1)).createTodo(org.mockito.ArgumentMatchers.any(TodoRequest.class));
    }

    @Test
    void testUpdateTodo() throws Exception {
        TodoResponse updatedResponse = TodoResponse.builder()
                .id(1L)
                .title("Updated Todo")
                .description("Updated Description")
                .completed(true)
                .disabled(false)
                .ownerId(1L)
                .ownerUsername("user1")
                .folderId(1L)
                .folderName("Folder 1")
                .createdAt(todoResponse1.getCreatedAt())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(todoService.updateTodo(eq(1L), org.mockito.ArgumentMatchers.any(TodoRequest.class))).thenReturn(updatedResponse);

        TodoRequest updateRequest = TodoRequest.builder()
                .title("Updated Todo")
                .description("Updated Description")
                .completed(true)
                .folderId(1L)
                .build();

        mockMvc.perform(put("/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Todo")))
                .andExpect(jsonPath("$.completed", is(true)));

        verify(todoService, times(1)).updateTodo(eq(1L), org.mockito.ArgumentMatchers.any(TodoRequest.class));
    }

    @Test
    void testToggleCompleted() throws Exception {
        TodoResponse toggledResponse = TodoResponse.builder()
                .id(1L)
                .title("Test Todo 1")
                .description("Test Description 1")
                .completed(true)  // 已切换为完成状态
                .disabled(false)
                .ownerId(1L)
                .ownerUsername("user1")
                .folderId(1L)
                .folderName("Folder 1")
                .createdAt(todoResponse1.getCreatedAt())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(todoService.toggleCompleted(1L)).thenReturn(toggledResponse);

        mockMvc.perform(patch("/todos/1/toggle-completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.completed", is(true)));

        verify(todoService, times(1)).toggleCompleted(1L);
    }
    
    @Test
    void testToggleDisabledStatus() throws Exception {
        when(todoService.toggleTodoDisabledStatus(1L)).thenReturn(disabledResponse);

        mockMvc.perform(put("/todos/1/toggle-disabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Todo successfully disabled")));

        verify(todoService, times(1)).toggleTodoDisabledStatus(1L);
    }
    
    @Test
    void testToggleDisabledStatusNotFound() throws Exception {
        when(todoService.toggleTodoDisabledStatus(999L)).thenThrow(new ResourceNotFoundException("Todo not found with id: 999"));

        mockMvc.perform(put("/todos/999/toggle-disabled"))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).toggleTodoDisabledStatus(999L);
    }

    @Test
    void testDeleteTodo() throws Exception {
        mockMvc.perform(delete("/todos/1"))
                .andExpect(status().isNoContent());

        verify(todoService, times(1)).deleteTodo(1L);
    }
} 