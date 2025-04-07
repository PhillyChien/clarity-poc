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
public class FolderResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerUsername;
    private int todoCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
} 