package com.aifinancial.clarity.poc.service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import static org.mockito.quality.Strictness.LENIENT;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.aifinancial.clarity.poc.dto.request.FolderRequest;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.exception.UnauthorizedException;
import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Permission;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.FolderRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.security.UserDetailsImpl;
import com.aifinancial.clarity.poc.service.impl.FolderServiceImpl;
import com.aifinancial.clarity.poc.constant.RoleConstants;

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
    private Role normalRoleEntity;
    private Role adminRoleEntity;
    private UserDetailsImpl normalUserDetails;
    private UserDetailsImpl adminUserDetails;

    private Collection<? extends GrantedAuthority> buildAuthorities(Role role) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        if (role.getPermissions() != null) {
            role.getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName()))
            );
        }
        return authorities;
    }

    @BeforeEach
    void setUp() {
        Permission folderRead = new Permission("folder:read");
        Permission folderWrite = new Permission("folder:write");
        Permission folderDelete = new Permission("folder:delete");
        Permission allPermission = new Permission("*");

        normalRoleEntity = new Role(1L, RoleConstants.ROLE_NORMAL,
                                     new HashSet<>(Arrays.asList(folderRead, folderWrite, folderDelete)));
        adminRoleEntity = new Role(2L, RoleConstants.ROLE_SUPER_ADMIN,
                                    new HashSet<>(Arrays.asList(allPermission)));

        normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("normal_user");
        normalUser.setEmail("normal@example.com");
        normalUser.setPassword("password");
        normalUser.setRole(normalRoleEntity);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin_user");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setRole(adminRoleEntity);

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

        Collection<? extends GrantedAuthority> normalAuthorities = buildAuthorities(normalRoleEntity);
        Collection<? extends GrantedAuthority> adminAuthorities = buildAuthorities(adminRoleEntity);

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

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        when(folderRepository.findById(folder1.getId())).thenReturn(Optional.of(folder1));
        when(folderRepository.findById(folder2.getId())).thenReturn(Optional.of(folder2));
        when(folderRepository.findByOwnerOrderByCreatedAtDesc(normalUser)).thenReturn(Arrays.asList(folder1));
        when(folderRepository.findByOwnerOrderByCreatedAtDesc(adminUser)).thenReturn(Arrays.asList(folder2));
    }

    @Test
    void testGetCurrentUserFolders() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);

        List<FolderResponse> result = folderService.getCurrentUserFolders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(folder1.getId(), result.get(0).getId());
        assertEquals(normalUser.getId(), result.get(0).getOwnerId());
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, times(1)).findByOwnerOrderByCreatedAtDesc(normalUser);
    }

    @Test
    void testCreateFolder() {
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

        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(folderRepository.save(any(Folder.class))).thenReturn(newFolder);

        FolderResponse result = folderService.createFolder(request);

        assertNotNull(result);
        assertEquals(newFolder.getId(), result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(normalUser.getId(), result.getOwnerId());
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, times(1)).save(any(Folder.class));
    }

    @Test
    void testUpdateFolder_Owner() {
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

        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(folderRepository.save(any(Folder.class))).thenReturn(updatedFolder);

        FolderResponse result = folderService.updateFolder(folder1.getId(), request);

        assertNotNull(result);
        assertEquals(updatedFolder.getId(), result.getId());
        assertEquals(request.getName(), result.getName());
        verify(folderRepository, times(1)).findById(folder1.getId());
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, times(1)).save(any(Folder.class));
    }

    @Test
    void testUpdateFolder_Unauthorized() {
        FolderRequest request = new FolderRequest();
        request.setName("Updated Folder");
        request.setDescription("Updated Description");

        when(authentication.getPrincipal()).thenReturn(normalUserDetails);

        assertThrows(UnauthorizedException.class, () -> {
            folderService.updateFolder(folder2.getId(), request);
        });

        verify(folderRepository, times(1)).findById(folder2.getId());
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, never()).save(any(Folder.class));
    }

    @Test
    void testDeleteFolder_Owner() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);

        folderService.deleteFolder(folder1.getId());

        verify(folderRepository, times(1)).findById(folder1.getId());
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, times(1)).delete(folder1);
    }

    @Test
    void testDeleteFolder_Unauthorized() {
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);

        assertThrows(UnauthorizedException.class, () -> {
            folderService.deleteFolder(folder2.getId());
        });

        verify(folderRepository, times(1)).findById(folder2.getId());
        verify(userRepository, times(1)).findById(normalUser.getId());
        verify(folderRepository, never()).delete(any(Folder.class));
    }

    @Test
    void testDeleteFolder_AdminDeletesOtherUserFolder() {
        when(authentication.getPrincipal()).thenReturn(adminUserDetails);

        assertDoesNotThrow(() -> {
            folderService.deleteFolder(folder1.getId());
        });

        verify(folderRepository, times(1)).findById(folder1.getId());
        verify(userRepository, times(1)).findById(adminUser.getId());
        verify(folderRepository, times(1)).delete(folder1);
    }

    @Test
    void testDeleteFolder_NotFound() {
        Long nonExistentFolderId = 999L;
        when(authentication.getPrincipal()).thenReturn(normalUserDetails);
        when(folderRepository.findById(nonExistentFolderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            folderService.deleteFolder(nonExistentFolderId);
        });

        verify(folderRepository, times(1)).findById(nonExistentFolderId);
        verify(folderRepository, never()).delete(any(Folder.class));
    }
} 