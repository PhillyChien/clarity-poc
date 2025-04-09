package com.aifinancial.clarity.poc.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwksResponse {
    private List<JwksKey> keys;
} 