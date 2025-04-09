package com.aifinancial.clarity.poc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwksKey {
    private String kid;  // Key ID
    private String kty;  // Key Type (e.g., "RSA")
    private String alg;  // Algorithm (e.g., "RS256")
    private String use;  // Use (e.g., "sig" for signature)
    private String n;    // Modulus
    private String e;    // Exponent
} 