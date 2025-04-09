package com.aifinancial.clarity.poc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aifinancial.clarity.poc.dto.request.FolderRequest;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;
import com.aifinancial.clarity.poc.service.FolderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/folders")
@Tag(name = "Folders", description = "Folder management operations")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get folders", 
               description = "Retrieves folders based on query parameters. If userId is provided and the user has proper permissions, returns folders for that user. Otherwise returns current user's folders.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Folders retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FolderResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<FolderResponse>> getFolders(
            @Parameter(description = "Optional user ID to filter folders by owner") 
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            // This requires moderator or admin privileges, which is checked in the service
            return ResponseEntity.ok(folderService.getFoldersByUserId(userId));
        } else {
            // Otherwise, return current user's folders
            return ResponseEntity.ok(folderService.getCurrentUserFolders());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get a folder by ID", 
               description = "Retrieves a specific folder by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Folder retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FolderResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Folder not found")
    })
    public ResponseEntity<FolderResponse> getFolder(
            @Parameter(description = "ID of the folder to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(folderService.getFolder(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new folder", 
               description = "Creates a new folder for the current user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Folder created successfully",
                    content = @Content(schema = @Schema(implementation = FolderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<FolderResponse> createFolder(
            @Parameter(description = "Folder details", required = true)
            @Valid @RequestBody FolderRequest folderRequest) {
        return ResponseEntity.ok(folderService.createFolder(folderRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update a folder", 
               description = "Updates an existing folder")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Folder updated successfully",
                    content = @Content(schema = @Schema(implementation = FolderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Folder not found")
    })
    public ResponseEntity<FolderResponse> updateFolder(
            @Parameter(description = "ID of the folder to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated folder details", required = true)
            @Valid @RequestBody FolderRequest folderRequest) {
        return ResponseEntity.ok(folderService.updateFolder(id, folderRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('NORMAL') or hasRole('MODERATOR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a folder", 
               description = "Deletes a folder by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Folder deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Login required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Folder not found")
    })
    public ResponseEntity<Void> deleteFolder(
            @Parameter(description = "ID of the folder to delete", required = true)
            @PathVariable Long id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }
} 