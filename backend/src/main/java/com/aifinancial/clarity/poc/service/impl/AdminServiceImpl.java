package com.aifinancial.clarity.poc.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aifinancial.clarity.poc.constant.RoleConstants;
import com.aifinancial.clarity.poc.converter.UserConverter;
import com.aifinancial.clarity.poc.dto.request.RoleUpdateRequest;
import com.aifinancial.clarity.poc.dto.response.MessageResponse;
import com.aifinancial.clarity.poc.dto.response.UserResponse;
import com.aifinancial.clarity.poc.exception.BadRequestException;
import com.aifinancial.clarity.poc.exception.ResourceNotFoundException;
import com.aifinancial.clarity.poc.model.Role;
import com.aifinancial.clarity.poc.model.User;
import com.aifinancial.clarity.poc.repository.RoleRepository;
import com.aifinancial.clarity.poc.repository.UserRepository;
import com.aifinancial.clarity.poc.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserConverter userConverter;

    public AdminServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        // Find the role entity by name
        String requestedRoleName = request.getRole().toUpperCase();
        Role newRole = roleRepository.findByName(requestedRoleName)
                .orElseThrow(() -> new BadRequestException("Invalid role: " + request.getRole()));

        // Prevent promotion to SUPER_ADMIN by comparing names using the constant
        if (RoleConstants.ROLE_SUPER_ADMIN.equalsIgnoreCase(newRole.getName())) {
            throw new AccessDeniedException("Cannot promote users to SUPER_ADMIN role");
        }

        // Update user role using the Role entity
        user.setRole(newRole);
        userRepository.save(user);

        // Use role name from the entity in the response message
        return new MessageResponse("User role updated successfully to " + newRole.getName());
    }
} 