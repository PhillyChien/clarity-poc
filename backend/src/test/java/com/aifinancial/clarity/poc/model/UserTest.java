package com.aifinancial.clarity.poc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    public void testUserBuilder() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.NORMAL)
                .build();
        
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(Role.NORMAL, user.getRole());
    }
} 