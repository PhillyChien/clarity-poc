package com.aifinancial.clarity.poc.service;

import com.aifinancial.clarity.poc.dto.request.FolderRequest;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.exception.UnauthorizedException;
import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.FolderRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.security.UserDetailsImpl;
import com.aifinancial.clarity.poc.service.impl.FolderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FolderServiceImpl folderService;

    private User normalUser;
    private User adminUser;
    private Folder folder1;
    private Folder folder2;
    private UserDetailsImpl normalUserDetails;
    private UserDetailsImpl adminUserDetails;
    private List<SimpleGrantedAuthority> normalAuthorities;
    private List<SimpleGrantedAuthority> adminAuthorities;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("normal_user");
        normalUser.setEmail("normal@example.com");
        normalUser.setPassword("password");
        normalUser.setRole(Role.NORMAL);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin_user");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setRole(Role.SUPER_ADMIN);

        // 创建测试文件夹
        folder1 = new Folder();
        folder1.setId(1L);
        folder1.setName("Test Folder 1");
        folder1.setDescription("Test Description 1");
        folder1.setOwner(normalUser);
        folder1.setCreatedAt(OffsetDateTime.now());
        folder1.setUpdatedAt(OffsetDateTime.now());

        folder2 = new Folder();
        folder2.setId(2L);
        folder2.setName("Test Folder 2");
        folder2.setDescription("Test Description 2");
        folder2.setOwner(adminUser);
        folder2.setCreatedAt(OffsetDateTime.now());
        folder2.setUpdatedAt(OffsetDateTime.now());

        // 创建权限列表
        normalAuthorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_NORMAL"));
        adminAuthorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));

        // 创建UserDetails对象
        normalUserDetails = new UserDetailsImpl(
                normalUser.getId(),
                normalUser.getUsername(),
                normalUser.getEmail(),
                normalUser.getPassword(),
                normalAuthorities
        );

        adminUserDetails = new UserDetailsImpl(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getEmail(),
                adminUser.getPassword(),
                adminAuthorities
        );

        // 设置SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetCurrentUserFolders() {
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(folderRepository.findByOwnerOrderByCreatedAtDesc(normalUser)).thenReturn(Arrays.asList(folder1));

        // 执行测试
        List<FolderResponse> result = folderService.getCurrentUserFolders();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(folder1.getId(), result.get(0).getId());
        assertEquals(folder1.getName(), result.get(0).getName());
        assertEquals(folder1.getDescription(), result.get(0).getDescription());
        assertEquals(normalUser.getId(), result.get(0).getOwnerId());
        assertEquals(normalUser.getUsername(), result.get(0).getOwnerUsername());
    }

    @Test
    void testGetAllFoldersAsAdmin() {
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);
        
        // 使用doReturn().when()模式代替when().thenReturn()
        doReturn(adminAuthorities).when(authentication).getAuthorities();
        
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(folderRepository.findAll()).thenReturn(Arrays.asList(folder1, folder2));

        // 执行测试
        List<FolderResponse> result = folderService.getAllFolders();

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetAllFoldersAsNormalUser() {
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        // 使用doReturn().when()模式代替when().thenReturn()
        doReturn(normalAuthorities).when(authentication).getAuthorities();

        // 验证正常用户无法获取所有文件夹
        assertThrows(UnauthorizedException.class, () -> folderService.getAllFolders());
    }

    @Test
    void testCreateFolder() {
        // 准备测试数据
        FolderRequest request = new FolderRequest();
        request.setName("New Folder");
        request.setDescription("New Description");

        Folder newFolder = new Folder();
        newFolder.setId(3L);
        newFolder.setName(request.getName());
        newFolder.setDescription(request.getDescription());
        newFolder.setOwner(normalUser);
        newFolder.setCreatedAt(OffsetDateTime.now());
        newFolder.setUpdatedAt(OffsetDateTime.now());

        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(folderRepository.save(any(Folder.class))).thenReturn(newFolder);

        // 执行测试
        FolderResponse result = folderService.createFolder(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(newFolder.getId(), result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(normalUser.getId(), result.getOwnerId());
    }

    @Test
    void testUpdateFolder() {
        // 准备测试数据
        FolderRequest request = new FolderRequest();
        request.setName("Updated Folder");
        request.setDescription("Updated Description");

        Folder updatedFolder = new Folder();
        updatedFolder.setId(folder1.getId());
        updatedFolder.setName(request.getName());
        updatedFolder.setDescription(request.getDescription());
        updatedFolder.setOwner(normalUser);
        updatedFolder.setCreatedAt(folder1.getCreatedAt());
        updatedFolder.setUpdatedAt(OffsetDateTime.now());

        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(folderRepository.findById(folder1.getId())).thenReturn(Optional.of(folder1));
        when(folderRepository.save(any(Folder.class))).thenReturn(updatedFolder);

        // 执行测试
        FolderResponse result = folderService.updateFolder(folder1.getId(), request);

        // 验证结果
        assertNotNull(result);
        assertEquals(updatedFolder.getId(), result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getDescription(), result.getDescription());
    }

    @Test
    void testDeleteFolder() {
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(folderRepository.findById(folder1.getId())).thenReturn(Optional.of(folder1));

        // 执行测试
        folderService.deleteFolder(folder1.getId());

        // 验证文件夹是否被删除
        verify(folderRepository, times(1)).delete(folder1);
    }

    @Test
    void testDeleteFolderUnauthorized() {
        // 尝试删除不属于当前用户的文件夹
        // 模拟安全上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        
        // 使用doReturn().when()模式代替when().thenReturn()
        doReturn(normalAuthorities).when(authentication).getAuthorities();
        
        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(folderRepository.findById(folder2.getId())).thenReturn(Optional.of(folder2));

        // 验证没有权限的用户不能删除其他用户的文件夹
        assertThrows(UnauthorizedException.class, () -> folderService.deleteFolder(folder2.getId()));
    }
} 