package com.aifinancial.clarity.poc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 Web MVC 配置
 * 這個類會統一處理所有控制器的 CORS 設置，取代在每個控制器上單獨添加 @CrossOrigin
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置全局 CORS 規則
     * @param registry CORS 註冊表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Content-Type")
            .exposedHeaders("Authorization")
            .allowCredentials(true)
            .maxAge(3600); // 1小時的預檢請求緩存
    }
} 