package com.aifinancial.clarity.poc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoResponse {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private boolean disabled;
    private Long ownerId;
    private String ownerUsername;
    private Long folderId;
    private String folderName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
} 