package com.aifinancial.clarity.poc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import com.aifinancial.clarity.poc.constant.RoleConstants;

public class UserTest {

    @Test
    public void testUserBuilder() {
        Role normalRoleEntity = new Role(1L, RoleConstants.ROLE_NORMAL, new HashSet<>());
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(normalRoleEntity);
        
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(normalRoleEntity, user.getRole());
    }
} 