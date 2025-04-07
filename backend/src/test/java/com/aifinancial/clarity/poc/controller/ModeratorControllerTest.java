package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.TodoResponse;
import com.aifinancial.clarity.poc.service.ModeratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class ModeratorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ModeratorService moderatorService;

    @InjectMocks
    private ModeratorController moderatorController;

    private ObjectMapper objectMapper = new ObjectMapper();
    
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
        // 初始化 MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(moderatorController)
                .build();
        
        // 配置 ObjectMapper 以處理 Java 8 日期時間類型
        objectMapper.findAndRegisterModules();
        
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
    void testGetAllFolders() throws Exception {
        when(moderatorService.getAllFolders()).thenReturn(folderResponses);

        mockMvc.perform(get("/moderator/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Folder 1")))
                .andExpect(jsonPath("$[0].ownerId", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Test Folder 2")))
                .andExpect(jsonPath("$[1].ownerId", is(2)));

        verify(moderatorService, times(1)).getAllFolders();
    }

    @Test
    void testGetAllTodos() throws Exception {
        when(moderatorService.getAllTodos()).thenReturn(todoResponses);

        mockMvc.perform(get("/moderator/todos"))
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

        verify(moderatorService, times(1)).getAllTodos();
    }

    @Test
    void testToggleTodoStatusToDisable() throws Exception {
        when(moderatorService.toggleTodoDisabledStatus(1L)).thenReturn(disabledResponse);

        mockMvc.perform(put("/moderator/todos/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Todo successfully disabled")));

        verify(moderatorService, times(1)).toggleTodoDisabledStatus(1L);
    }

    @Test
    void testToggleTodoStatusToEnable() throws Exception {
        when(moderatorService.toggleTodoDisabledStatus(3L)).thenReturn(enabledResponse);

        mockMvc.perform(put("/moderator/todos/3/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Todo successfully enabled")));

        verify(moderatorService, times(1)).toggleTodoDisabledStatus(3L);
    }

    @Test
    void testToggleTodoStatusNotFound() throws Exception {
        when(moderatorService.toggleTodoDisabledStatus(999L)).thenReturn(new MessageResponse("Failed to toggle todo status: Todo not found with ID: 999"));

        mockMvc.perform(put("/moderator/todos/999/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Failed to toggle todo status: Todo not found with ID: 999")));

        verify(moderatorService, times(1)).toggleTodoDisabledStatus(999L);
    }
} 