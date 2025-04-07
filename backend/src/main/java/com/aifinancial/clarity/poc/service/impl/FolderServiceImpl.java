package com.aifinancial.clarity.poc.service.impl;

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
import com.aifinancial.clarity.poc.service.FolderService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public FolderServiceImpl(FolderRepository folderRepository, UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isCurrentUserModeratorOrAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR") || 
                        a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getAllFolders() {
        // Only moderators and admins can see all folders
        if (!isCurrentUserModeratorOrAdmin()) {
            throw new UnauthorizedException("Not authorized to view all folders");
        }

        return folderRepository.findAll().stream()
                .map(this::mapToFolderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getCurrentUserFolders() {
        User currentUser = getCurrentUser();
        return folderRepository.findByOwnerOrderByCreatedAtDesc(currentUser).stream()
                .map(this::mapToFolderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FolderResponse getFolder(Long id) {
        User currentUser = getCurrentUser();
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + id));

        // Check if the user is owner or moderator/admin
        if (!folder.getOwner().getId().equals(currentUser.getId()) && !isCurrentUserModeratorOrAdmin()) {
            throw new UnauthorizedException("Not authorized to view this folder");
        }

        return mapToFolderResponse(folder);
    }

    @Override
    @Transactional
    public FolderResponse createFolder(FolderRequest folderRequest) {
        User currentUser = getCurrentUser();
        
        Folder folder = new Folder();
        folder.setName(folderRequest.getName());
        folder.setDescription(folderRequest.getDescription());
        folder.setOwner(currentUser);
        
        folder = folderRepository.save(folder);
        return mapToFolderResponse(folder);
    }

    @Override
    @Transactional
    public FolderResponse updateFolder(Long id, FolderRequest folderRequest) {
        User currentUser = getCurrentUser();
        
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + id));
        
        // Check if the current user is the owner
        if (!folder.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Not authorized to update this folder");
        }
        
        folder.setName(folderRequest.getName());
        folder.setDescription(folderRequest.getDescription());
        
        folder = folderRepository.save(folder);
        return mapToFolderResponse(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(Long id) {
        User currentUser = getCurrentUser();
        
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + id));
        
        // Check if the current user is the owner or admin
        if (!folder.getOwner().getId().equals(currentUser.getId()) && 
                !currentUser.getRole().equals(Role.SUPER_ADMIN)) {
            throw new UnauthorizedException("Not authorized to delete this folder");
        }
        
        folderRepository.delete(folder);
    }
    
    private FolderResponse mapToFolderResponse(Folder folder) {
        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .description(folder.getDescription())
                .ownerId(folder.getOwner().getId())
                .ownerUsername(folder.getOwner().getUsername())
                .todoCount(folder.getTodos().size())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
} 