package com.aifinancial.clarity.poc.converter;

import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.constant.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserConverterTest {

    private UserConverter userConverter;
    private Role normalRoleEntity;
    private Role moderatorRoleEntity;
    private Role superAdminRoleEntity;
    private User normalUser;
    private User moderatorUser;
    private User adminUser;
    private List<User> users;

    @BeforeEach
    void setUp() {
        userConverter = new UserConverter();

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

        adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("admin_user");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setRole(superAdminRoleEntity);
        adminUser.setCreatedAt(OffsetDateTime.now());
        adminUser.setUpdatedAt(OffsetDateTime.now());

        users = Arrays.asList(normalUser, moderatorUser, adminUser);
    }

    @Test
    void testToDto() {
        UserResponse result = userConverter.toDto(normalUser);

        assertNotNull(result);
        assertEquals(normalUser.getId(), result.getId());
        assertEquals(normalUser.getUsername(), result.getUsername());
        assertEquals(normalUser.getEmail(), result.getEmail());
        assertEquals(RoleConstants.ROLE_NORMAL, result.getRole());
        assertEquals(normalUser.getCreatedAt(), result.getCreatedAt());
        assertEquals(normalUser.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void testToDtoList() {
        List<UserResponse> results = userConverter.toDtoList(users);

        assertNotNull(results);
        assertEquals(3, results.size());

        assertEquals(normalUser.getId(), results.get(0).getId());
        assertEquals(normalUser.getUsername(), results.get(0).getUsername());
        assertEquals(normalUser.getEmail(), results.get(0).getEmail());
        assertEquals(RoleConstants.ROLE_NORMAL, results.get(0).getRole());
        assertEquals(normalUser.getCreatedAt(), results.get(0).getCreatedAt());
        assertEquals(normalUser.getUpdatedAt(), results.get(0).getUpdatedAt());

        assertEquals(moderatorUser.getId(), results.get(1).getId());
        assertEquals(moderatorUser.getUsername(), results.get(1).getUsername());
        assertEquals(RoleConstants.ROLE_MODERATOR, results.get(1).getRole());

        assertEquals(adminUser.getId(), results.get(2).getId());
        assertEquals(adminUser.getUsername(), results.get(2).getUsername());
        assertEquals(RoleConstants.ROLE_SUPER_ADMIN, results.get(2).getRole());
    }

    @Test
    void testToDtoWithNullUser() {
        assertThrows(NullPointerException.class, () -> userConverter.toDto(null));
    }

    @Test
    void testToDtoListWithNullList() {
        assertThrows(NullPointerException.class, () -> userConverter.toDtoList(null));
    }

    @Test
    void testToDtoListWithEmptyList() {
        List<UserResponse> results = userConverter.toDtoList(List.of());
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
} 