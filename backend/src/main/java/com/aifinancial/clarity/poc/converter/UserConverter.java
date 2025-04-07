package com.aifinancial.clarity.poc.converter;

import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserConverter {

    /**
     * 將用戶實體轉換為用戶響應 DTO
     * @param user 用戶實體
     * @return 用戶響應 DTO
     */
    public UserResponse toDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 將用戶實體列表轉換為用戶響應 DTO 列表
     * @param users 用戶實體列表
     * @return 用戶響應 DTO 列表
     */
    public List<UserResponse> toDtoList(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
} 