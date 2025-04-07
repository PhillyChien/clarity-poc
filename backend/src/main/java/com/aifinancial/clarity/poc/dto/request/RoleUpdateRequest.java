package com.aifinancial.clarity.poc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    
    @NotBlank(message = "Role cannot be blank")
    private String role;
} 