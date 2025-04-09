package com.aifinancial.clarity.poc.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;
    
    @Value("${jwt.rsa.private-key:}")
    private String rsaPrivateKeyString;
    
    @Value("${jwt.rsa.public-key:}")
    private String rsaPublicKeyString;
    
    @Value("${jwt.rsa.private-key-file:}")
    private String rsaPrivateKeyFile;
    
    @Value("${jwt.rsa.public-key-file:}")
    private String rsaPublicKeyFile;
    
    @Value("${jwt.kid:}")
    private String configuredKid;

    private RSAPrivateKey rsaPrivateKey;
    private RSAPublicKey rsaPublicKey;
    private String kid;

    private final ResourceLoader resourceLoader;
    
    public JwtTokenProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        
        // Initialize RSA keys
        initializeRSAKeys();
    }
    
    private void initializeRSAKeys() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            boolean keysInitialized = false;
            
            // Check for environment variables first
            String envPrivateKeyFile = System.getenv("JWT_RSA_PRIVATE_KEY_FILE");
            String envPublicKeyFile = System.getenv("JWT_RSA_PUBLIC_KEY_FILE");
            String envKid = System.getenv("JWT_KID");
            
            // If environment variables exist, use them instead of @Value injected values
            if (envPrivateKeyFile != null && !envPrivateKeyFile.isEmpty()) {
                rsaPrivateKeyFile = envPrivateKeyFile;
            } else if (rsaPrivateKeyFile == null || rsaPrivateKeyFile.isEmpty()) {
                // Default to classpath resource if not specified
                rsaPrivateKeyFile = "classpath:keys/private_key.pem";
            }
            
            if (envPublicKeyFile != null && !envPublicKeyFile.isEmpty()) {
                rsaPublicKeyFile = envPublicKeyFile;
            } else if (rsaPublicKeyFile == null || rsaPublicKeyFile.isEmpty()) {
                // Default to classpath resource if not specified
                rsaPublicKeyFile = "classpath:keys/public_key.pem";
            }
            
            if (envKid != null && !envKid.isEmpty()) {
                configuredKid = envKid;
            } else if (configuredKid == null || configuredKid.isEmpty()) {
                // Default KID if not specified
                configuredKid = "646b2b4576e3e06abfcee95c8e7d19f2";
            }
            
            // Try to load keys from PEM files
            if (!keysInitialized && StringUtils.hasText(rsaPrivateKeyFile) && StringUtils.hasText(rsaPublicKeyFile)) {
                try {
                    // Load private key from PEM file
                    String privateKeyPem = readPemFile(rsaPrivateKeyFile);
                    
                    byte[] privateKeyBytes = pemToEncodedKey(privateKeyPem, true);
                    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                    this.rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
                    
                    // Load public key from PEM file
                    String publicKeyPem = readPemFile(rsaPublicKeyFile);
                    
                    byte[] publicKeyBytes = pemToEncodedKey(publicKeyPem, false);
                    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                    this.rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
                    
                    // Use configured KID
                    this.kid = configuredKid;
                    
                    keysInitialized = true;
                } catch (Exception e) {
                    // 失敗時繼續嘗試下一種方法
                }
            }
            
            // Try to load keys from Base64 strings
            if (!keysInitialized && StringUtils.hasText(rsaPrivateKeyString) && StringUtils.hasText(rsaPublicKeyString)) {
                try {
                    // Load private key from Base64 encoded string
                    byte[] privateKeyBytes = Base64.getDecoder().decode(rsaPrivateKeyString);
                    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                    this.rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
                    
                    // Load public key from Base64 encoded string
                    byte[] publicKeyBytes = Base64.getDecoder().decode(rsaPublicKeyString);
                    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                    this.rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
                    
                    // Use configured KID or generate a new one
                    this.kid = configuredKid.isEmpty() ? UUID.randomUUID().toString() : configuredKid;
                    
                    keysInitialized = true;
                } catch (Exception e) {
                    // 失敗時繼續
                }
            }
            
            if (!keysInitialized) {
                throw new RuntimeException("No RSA keys were configured. Please provide RSA keys through configuration.");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RSA keys: " + e.getMessage(), e);
        }
    }
    
    /**
     * Read a PEM format key file, supporting both classpath and file system resources
     */
    private String readPemFile(String filePath) throws IOException {
        try {
            if (filePath.startsWith("classpath:")) {
                // Handle classpath resources
                String resourcePath = filePath.substring("classpath:".length());
                Resource resource = new ClassPathResource(resourcePath);
                byte[] keyBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                return new String(keyBytes);
            } else {
                // Handle file system resources
                return new String(Files.readAllBytes(Paths.get(filePath)));
            }
        } catch (IOException e) {
            throw new IOException("Failed to read key file: " + filePath, e);
        }
    }
    
    /**
     * Convert a PEM format key to binary encoding
     * @param pemKey PEM format key
     * @param isPrivate Whether it's a private key
     * @return Encoded key byte array
     */
    private byte[] pemToEncodedKey(String pemKey, boolean isPrivate) {
        // Remove header and footer marker lines
        String beginMarker = isPrivate ? "-----BEGIN PRIVATE KEY-----" : "-----BEGIN PUBLIC KEY-----";
        String endMarker = isPrivate ? "-----END PRIVATE KEY-----" : "-----END PUBLIC KEY-----";
        
        // Remove marker lines and all whitespace
        Pattern pattern = Pattern.compile("\\s+");
        String encodedKey = pemKey
                .replace(beginMarker, "")
                .replace(endMarker, "");
        encodedKey = pattern.matcher(encodedKey).replaceAll("");
        
        // Decode Base64 string
        return Base64.getDecoder().decode(encodedKey);
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
            return Jwts.parserBuilder()
                    .setSigningKey(rsaPublicKey)
                    .build()
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
        // Extract modulus (n) and exponent (e) from public key
        String modulus = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rsaPublicKey.getModulus().toByteArray());
        String exponent = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rsaPublicKey.getPublicExponent().toByteArray());
        
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