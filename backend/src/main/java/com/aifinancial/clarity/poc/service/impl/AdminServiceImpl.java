package com.aifinancial.clarity.poc.service.impl;

import com.aifinancial.clarity.poc.converter.UserConverter;
import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.service.AdminService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    public AdminServiceImpl(UserRepository userRepository, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userConverter.toDtoList(users);
    }

    @Override
    @Transactional
    public MessageResponse updateUserRole(RoleUpdateRequest request) {
        try {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));

            // 檢查請求的角色是否有效
            Role newRole;
            try {
                newRole = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return new MessageResponse("Invalid role: " + request.getRole());
            }

            // 不允許將用戶提升為 SUPER_ADMIN
            if (newRole == Role.SUPER_ADMIN) {
                throw new AccessDeniedException("Cannot promote users to SUPER_ADMIN role");
            }

            // 更新用戶角色
            user.setRole(newRole);
            userRepository.save(user);

            return new MessageResponse("User role updated successfully to " + newRole.name());
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return new MessageResponse("Failed to update user role: " + e.getMessage());
        }
    }
} 