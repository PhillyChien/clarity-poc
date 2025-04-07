package com.aifinancial.clarity.poc.controller;

import com.aifinancial.clarity.poc.dto.request.FolderRequest;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.service.FolderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<FolderResponse>> getCurrentUserFolders() {
        return ResponseEntity.ok(folderService.getCurrentUserFolders());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<FolderResponse>> getAllFolders() {
        return ResponseEntity.ok(folderService.getAllFolders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<FolderResponse> getFolder(@PathVariable Long id) {
        return ResponseEntity.ok(folderService.getFolder(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<FolderResponse> createFolder(@Valid @RequestBody FolderRequest folderRequest) {
        return ResponseEntity.ok(folderService.createFolder(folderRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<FolderResponse> updateFolder(
            @PathVariable Long id,
            @Valid @RequestBody FolderRequest folderRequest) {
        return ResponseEntity.ok(folderService.updateFolder(id, folderRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }
} 