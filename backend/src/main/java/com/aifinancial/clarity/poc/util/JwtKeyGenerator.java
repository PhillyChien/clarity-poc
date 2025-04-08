package com.aifinancial.clarity.poc.util;

import java.util.Base64;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JWT Key Generator
 */
public class JwtKeyGenerator {

    public static void main(String[] args) {
        // Generate a secure key for the HS512 algorithm
        byte[] key = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        // Encode the key using Base64 for easier storage
        String encodedKey = Base64.getEncoder().encodeToString(key);
        
        System.out.println("Generated secure JWT key (please save it to the environment variable):");
        System.out.println(encodedKey);
        System.out.println("\nConfiguration in application.yml:");
        System.out.println("jwt:");
        System.out.println("  secret: ${JWT_SECRET:" + encodedKey + "}");
    }
} 