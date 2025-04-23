package com.aifinancial.clarity.poc.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct; // Import jakarta annotation
import org.slf4j.Logger; // Optional: Add logging
import org.slf4j.LoggerFactory; // Optional: Add logging

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import com.aifinancial.clarity.poc.dto.response.JwksKey;
import com.aifinancial.clarity.poc.dto.response.JwksResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    // --- Properties for different loading strategies ---

    // Strategy 1: File paths (Highest priority if set)
    @Value("${jwt.rsa.private-key-file:}") // Read from application properties/env
    private String configuredPrivateKeyFile;
    @Value("${jwt.rsa.public-key-file:}") // Read from application properties/env
    private String configuredPublicKeyFile;

    // Strategy 2: Direct PEM content (e.g., from Key Vault)
    @Value("${jwt-rsa-private-key:}") // Tries to resolve directly (e.g., KV)
    private String keyVaultPrivateKeyPemContent;
    @Value("${jwt-rsa-public-key:}") // Tries to resolve directly (e.g., KV)
    private String keyVaultPublicKeyPemContent;

    // Strategy 3: Direct Base64 strings (Lowest priority)
    @Value("${jwt.rsa.private-key:}") // Read from application properties/env
    private String base64PrivateKeyString; // Should be raw Base64, no PEM headers
    @Value("${jwt.rsa.public-key:}") // Read from application properties/env
    private String base64PublicKeyString; // Should be raw Base64, no PEM headers

    // Key ID: Prioritize Key Vault, fallback to application property
    @Value("${jwt-kid:}") // Tries to resolve directly (e.g., KV)
    private String keyVaultKid;
    @Value("${jwt.kid:}") // Read from application properties/env (fallback)
    private String configuredKidFallback;


    private RSAPrivateKey rsaPrivateKey;
    private RSAPublicKey rsaPublicKey;
    private String kid; // The final kid to use

    private final ResourceLoader resourceLoader; // Needed again for file loading

    // Constructor requires ResourceLoader again
    public JwtTokenProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    @PostConstruct
    public void initializeRSAKeys() {
        boolean keysInitialized = false;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // --- Priority 1: Try loading from specified files ---
            if (StringUtils.hasText(configuredPrivateKeyFile) && StringUtils.hasText(configuredPublicKeyFile)) {
                logger.info("Attempting to load RSA keys from specified files: private='{}', public='{}'", configuredPrivateKeyFile, configuredPublicKeyFile);
                try {
                    String privateKeyPem = readPemFile(configuredPrivateKeyFile);
                    String publicKeyPem = readPemFile(configuredPublicKeyFile);

                    byte[] privateKeyBytes = pemToEncodedBytes(privateKeyPem, true);
                    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                    this.rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

                    byte[] publicKeyBytes = pemToEncodedBytes(publicKeyPem, false);
                    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                    this.rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

                    keysInitialized = true;
                    logger.info("Successfully loaded RSA keys from files.");
                } catch (Exception e) {
                    logger.warn("Failed to load keys from specified files ('{}', '{}'). Reason: {}. Falling back to other methods.",
                              configuredPrivateKeyFile, configuredPublicKeyFile, e.getMessage());
                    // Log full trace for debugging if needed: logger.debug("File loading stack trace:", e);
                }
            }

            // --- Priority 2: Try loading from direct PEM content (Key Vault) ---
            if (!keysInitialized && StringUtils.hasText(keyVaultPrivateKeyPemContent) && StringUtils.hasText(keyVaultPublicKeyPemContent)) {
                logger.info("Attempting to load RSA keys from direct PEM content (potentially Key Vault).");
                try {
                    // Assumes keyVaultPrivateKeyPemContent and keyVaultPublicKeyPemContent contain PEM formatted keys
                    byte[] privateKeyBytes = pemToEncodedBytes(keyVaultPrivateKeyPemContent, true);
                    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                    this.rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

                    byte[] publicKeyBytes = pemToEncodedBytes(keyVaultPublicKeyPemContent, false);
                    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                    this.rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

                    keysInitialized = true;
                    logger.info("Successfully loaded RSA keys from direct PEM content.");
                } catch (Exception e) {
                    logger.warn("Failed to load keys from direct PEM content. Reason: {}. Falling back to other methods.", e.getMessage());
                    // Log full trace for debugging if needed: logger.debug("Direct PEM content loading stack trace:", e);
                }
            }

            // --- Priority 3: Try loading from direct Base64 strings ---
            if (!keysInitialized && StringUtils.hasText(base64PrivateKeyString) && StringUtils.hasText(base64PublicKeyString)) {
                 logger.info("Attempting to load RSA keys from direct Base64 strings (jwt.rsa.private-key/public-key).");
                 try {
                     // Assumes base64PrivateKeyString/base64PublicKeyString are raw Base64, no PEM headers/footers
                     byte[] privateKeyBytes = Base64.getDecoder().decode(base64PrivateKeyString);
                     PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                     this.rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

                     byte[] publicKeyBytes = Base64.getDecoder().decode(base64PublicKeyString);
                     X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                     this.rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

                     keysInitialized = true;
                     logger.info("Successfully loaded RSA keys from direct Base64 strings.");
                 } catch (Exception e) {
                     logger.warn("Failed to load keys from direct Base64 strings. Reason: {}.", e.getMessage());
                     // Log full trace for debugging if needed: logger.debug("Base64 string loading stack trace:", e);
                 }
            }

            // --- Final Check ---
            if (!keysInitialized) {
                throw new RuntimeException("Failed to initialize RSA keys. No valid configuration found via files, direct content (Key Vault), or Base64 strings.");
            }

            // --- Initialize Key ID (KID) ---
            if (StringUtils.hasText(keyVaultKid)) {
                this.kid = keyVaultKid; // Prioritize Key Vault KID
                logger.info("Using Key ID (kid) from direct injection (Key Vault): {}", this.kid);
            } else if (StringUtils.hasText(configuredKidFallback)) {
                this.kid = configuredKidFallback; // Fallback to application property
                logger.info("Using Key ID (kid) from configuration property (jwt.kid): {}", this.kid);
            } else {
                logger.warn("No Key ID (kid) found from Key Vault ('jwt-kid') or configuration ('jwt.kid'). Consider setting one.");
                // Decide: throw error or generate default? Let's throw for explicitness.
                throw new IllegalArgumentException("Key ID (kid) is missing. Please configure 'jwt-kid' (Key Vault) or 'jwt.kid'.");
            }

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to get RSA KeyFactory instance: " + e.getMessage(), e);
        } catch (Exception e) { // Catch any other unexpected initialization errors
             logger.error("Unexpected error during RSA key initialization: {}", e.getMessage(), e);
             throw new RuntimeException("Unexpected error during RSA key initialization: " + e.getMessage(), e);
        }
    }

    /**
     * Reads a PEM format key file, supporting both classpath and file system resources.
     * (Restored from original logic)
     */
    private String readPemFile(String filePath) throws IOException {
        logger.debug("Reading PEM file from path: {}", filePath);
        Resource resource;
        if (filePath.startsWith("classpath:")) {
            // Handle classpath resources
            String resourcePath = filePath.substring("classpath:".length());
            resource = resourceLoader.getResource("classpath:" + resourcePath); // Use resourceLoader
            if (!resource.exists()) {
                throw new IOException("Classpath resource not found: " + resourcePath);
            }
        } else {
            // Handle file system resources using ResourceLoader for consistency (handles file:/ etc.)
            resource = resourceLoader.getResource(filePath);
             if (!resource.exists()) {
                  // Fallback: try direct path interpretation if resource loader fails for simple paths
                  try {
                      return new String(Files.readAllBytes(Paths.get(filePath)));
                  } catch (IOException fsEx) {
                      throw new IOException("File system resource not found or failed to read: " + filePath, fsEx);
                  }
             }
        }
        try {
             byte[] keyBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
             return new String(keyBytes);
        } catch (IOException e) {
            throw new IOException("Failed to read key file resource: " + filePath, e);
        }
    }


    /**
     * Converts a PEM format key string (including headers/footers) to binary encoded bytes.
     * (Same as before)
     */
    private byte[] pemToEncodedBytes(String pemKey, boolean isPrivate) {
        // Remove header and footer marker lines
        String beginMarker = isPrivate ? "-----BEGIN PRIVATE KEY-----" : "-----BEGIN PUBLIC KEY-----";
        String endMarker = isPrivate ? "-----END PRIVATE KEY-----" : "-----END PUBLIC KEY-----";

        // Remove marker lines and all whitespace (including newlines within the base64 block)
        Pattern pattern = Pattern.compile("\\s+");
        String encodedKey = pemKey
                .replace(beginMarker, "")
                .replace(endMarker, "");
        encodedKey = pattern.matcher(encodedKey).replaceAll(""); // Remove all whitespace

        try {
             // Decode Base64 string
             return Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to decode Base64 content for {} key. Content snippet: '{}'",
                         isPrivate ? "private" : "public",
                         encodedKey.substring(0, Math.min(encodedKey.length(), 50)) + "..."); // Log snippet
            throw new IllegalArgumentException("Invalid Base64 format in PEM key content", e);
        }
    }


    public String generateToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenFromUserDetails(userDetails);
    }
    
    public String generateTokenFromUserDetails(UserDetailsImpl userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("");

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userDetails.getId());
        claims.put("username", userDetails.getUsername());
        claims.put("email", userDetails.getEmail());
        claims.put("role", role);
        
        // Sign with RSA private key
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .signWith(rsaPrivateKey, SignatureAlgorithm.RS256)
                .setHeaderParam("kid", kid)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getSubject);
        } catch (Exception e) {
            return null; // Return null when token is invalid
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getExpiration);
        } catch (Exception e) {
            // Return a past date to indicate token expiration
            return new Date(0L);
        }
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            // Throw exception to let caller know claim retrieval failed
            throw e;
        }
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            // Use Jwts.parser() for jjwt 0.11.x
            return Jwts.parser() 
                    .setSigningKey(rsaPublicKey)
                    // Remove .build() as it's not part of the 0.11.x parser chain
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Throw exception when signature verification fails
            throw e;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            // Any exception means token is invalid
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            // If expiration time can't be retrieved, consider it expired
            return true;
        }
    }
    
    public JwksResponse getJwks() {
        if (this.rsaPublicKey == null || this.kid == null) {
            logger.error("JWKS endpoint called before RSA public key or KID was initialized.");
            return new JwksResponse();
        }
        byte[] modulusBytes = rsaPublicKey.getModulus().toByteArray();
        byte[] exponentBytes = rsaPublicKey.getPublicExponent().toByteArray();
        if (modulusBytes[0] == 0 && modulusBytes.length > 1) {
            byte[] tmp = new byte[modulusBytes.length - 1];
            System.arraycopy(modulusBytes, 1, tmp, 0, tmp.length);
            modulusBytes = tmp;
        }
        String modulus = Base64.getUrlEncoder().withoutPadding().encodeToString(modulusBytes);
        String exponent = Base64.getUrlEncoder().withoutPadding().encodeToString(exponentBytes);
        JwksKey key = new JwksKey();
        key.setKid(kid);
        key.setKty("RSA");
        key.setAlg("RS256");
        key.setUse("sig");
        key.setN(modulus);
        key.setE(exponent);
        JwksResponse jwks = new JwksResponse();
        jwks.setKeys(java.util.Collections.singletonList(key));
        return jwks;
    }
} 