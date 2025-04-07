package com.aifinancial.clarity.poc.converter;

import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserConverterTest {

    private UserConverter userConverter;
    private User normalUser;
    private User moderatorUser;
    private User adminUser;
    private List<User> users;

    @BeforeEach
    void setUp() {
        userConverter = new UserConverter();

        // 創建測試用戶
        normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("normal_user");
        normalUser.setEmail("normal@example.com");
        normalUser.setPassword("password");
        normalUser.setRole(Role.NORMAL);
        normalUser.setCreatedAt(OffsetDateTime.now());
        normalUser.setUpdatedAt(OffsetDateTime.now());

        moderatorUser = new User();
        moderatorUser.setId(2L);
        moderatorUser.setUsername("moderator_user");
        moderatorUser.setEmail("moderator@example.com");
        moderatorUser.setPassword("password");
        moderatorUser.setRole(Role.MODERATOR);
        moderatorUser.setCreatedAt(OffsetDateTime.now());
        moderatorUser.setUpdatedAt(OffsetDateTime.now());

        adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("admin_user");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setRole(Role.SUPER_ADMIN);
        adminUser.setCreatedAt(OffsetDateTime.now());
        adminUser.setUpdatedAt(OffsetDateTime.now());

        users = Arrays.asList(normalUser, moderatorUser, adminUser);
    }

    @Test
    void testToDto() {
        // 執行測試
        UserResponse result = userConverter.toDto(normalUser);

        // 驗證結果
        assertNotNull(result);
        assertEquals(normalUser.getId(), result.getId());
        assertEquals(normalUser.getUsername(), result.getUsername());
        assertEquals(normalUser.getEmail(), result.getEmail());
        assertEquals(normalUser.getRole().name(), result.getRole());
        assertEquals(normalUser.getCreatedAt(), result.getCreatedAt());
        assertEquals(normalUser.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void testToDtoList() {
        // 執行測試
        List<UserResponse> results = userConverter.toDtoList(users);

        // 驗證結果
        assertNotNull(results);
        assertEquals(3, results.size());

        // 驗證第一個用戶
        assertEquals(normalUser.getId(), results.get(0).getId());
        assertEquals(normalUser.getUsername(), results.get(0).getUsername());
        assertEquals(normalUser.getEmail(), results.get(0).getEmail());
        assertEquals(normalUser.getRole().name(), results.get(0).getRole());
        assertEquals(normalUser.getCreatedAt(), results.get(0).getCreatedAt());
        assertEquals(normalUser.getUpdatedAt(), results.get(0).getUpdatedAt());

        // 驗證第二個用戶
        assertEquals(moderatorUser.getId(), results.get(1).getId());
        assertEquals(moderatorUser.getUsername(), results.get(1).getUsername());
        assertEquals(moderatorUser.getRole().name(), results.get(1).getRole());

        // 驗證第三個用戶
        assertEquals(adminUser.getId(), results.get(2).getId());
        assertEquals(adminUser.getUsername(), results.get(2).getUsername());
        assertEquals(adminUser.getRole().name(), results.get(2).getRole());
    }

    @Test
    void testToDtoWithNullUser() {
        // 驗證傳入 null 時不會發生異常
        assertThrows(NullPointerException.class, () -> userConverter.toDto(null));
    }

    @Test
    void testToDtoListWithNullList() {
        // 驗證傳入 null 時不會發生異常
        assertThrows(NullPointerException.class, () -> userConverter.toDtoList(null));
    }

    @Test
    void testToDtoListWithEmptyList() {
        // 驗證傳入空列表時返回空列表
        List<UserResponse> results = userConverter.toDtoList(List.of());
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
} 