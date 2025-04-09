package com.aifinancial.clarity.poc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用戶信息響應DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeResponse {
    private String type;
    private Long id;
    private String username;
    private String email;
    private String role;
} 