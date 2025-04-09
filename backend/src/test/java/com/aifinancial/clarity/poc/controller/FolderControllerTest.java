package com.aifinancial.clarity.poc.controller;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import static org.mockito.quality.Strictness.LENIENT;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.exception.GlobalExceptionHandler;
import com.aifinancial.clarity.poc.service.FolderService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class FolderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FolderService folderService;

    @InjectMocks
    private FolderController folderController;

    private ObjectMapper objectMapper = new ObjectMapper();
    
    private FolderResponse folderResponse1;
    private FolderResponse folderResponse2;
    private List<FolderResponse> folderResponses;

    @BeforeEach
    void setUp() {
        // 初始化 MockMvc，添加全局异常处理器
        mockMvc = MockMvcBuilders.standaloneSetup(folderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // 配置 ObjectMapper 以处理 Java 8 日期时间类型
        objectMapper.findAndRegisterModules();
        
        // 创建测试文件夹响应对象
        folderResponse1 = FolderResponse.builder()
                .id(1L)
                .name("Test Folder 1")
                .description("Test Description 1")
                .ownerId(1L)
                .ownerUsername("user1")
                .todoCount(2)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        folderResponse2 = FolderResponse.builder()
                .id(2L)
                .name("Test Folder 2")
                .description("Test Description 2")
                .ownerId(2L)
                .ownerUsername("user2")
                .todoCount(1)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        folderResponses = Arrays.asList(folderResponse1, folderResponse2);
    }

    @Test
    void testGetCurrentUserFolders() throws Exception {
        when(folderService.getCurrentUserFolders()).thenReturn(folderResponses);

        mockMvc.perform(get("/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Folder 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Test Folder 2")));

        verify(folderService, times(1)).getCurrentUserFolders();
    }

    @Test
    void testGetFoldersByUserId() throws Exception {
        when(folderService.getFoldersByUserId(1L)).thenReturn(folderResponses);

        mockMvc.perform(get("/folders?userId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Folder 1")))
                .andExpect(jsonPath("$[0].ownerId", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Test Folder 2")))
                .andExpect(jsonPath("$[1].ownerId", is(2)));

        verify(folderService, times(1)).getFoldersByUserId(1L);
    }
} 