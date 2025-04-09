package com.aifinancial.clarity.poc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aifinancial.clarity.poc.dto.response.JwksResponse;
import com.aifinancial.clarity.poc.security.JwtTokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/.well-known")
@Tag(name = "JWKS", description = "JSON Web Key Set endpoints for JWT verification")
public class JwksController {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwksController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @GetMapping("/jwks.json")
    @Operation(summary = "Get JWKS", 
               description = "Returns the JSON Web Key Set for JWT verification")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "JWKS returned successfully", 
                     content = @Content(schema = @Schema(implementation = JwksResponse.class)))
    })
    public ResponseEntity<JwksResponse> getJwks() {
        JwksResponse jwks = jwtTokenProvider.getJwks();
        return ResponseEntity.ok(jwks);
    }
} 