package com.aifinancial.clarity.poc.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 Web MVC 配置
 * 這個類會統一處理所有控制器的 CORS 設置，取代在每個控制器上單獨添加 @CrossOrigin
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed_origins:${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,http://localhost:4173,http://localhost:80,http://localhost}}")
    private String[] allowedOrigins;

    /**
     * 配置全局 CORS 規則
     * @param registry CORS 註冊表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")
            .exposedHeaders("Authorization")
            .allowCredentials(true)
            .maxAge(3600); // 1小時的預檢請求緩存
    }
    
    /**
     * Spring Security 會使用的 CORS 配置源
     * 這個配置與上面的 addCorsMappings 保持一致
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 使用環境變數中的允許來源
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(java.util.List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 