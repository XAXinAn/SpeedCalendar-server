package com.example.speedcalendarserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 用于配置跨域资源共享 (CORS) 等 Web 相关设置
 *
 * @author SpeedCalendar Team
 * @since 2025-12-02
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域请求处理
     * 允许来自任意源的请求，以便于真机调试和前端开发
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有路径应用跨域配置
                .allowedOriginPatterns("*") // 允许所有来源（支持通配符）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // 允许的请求方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许携带凭证（如 Cookie）
                .maxAge(3600); // 预检请求缓存时间（秒）
    }
}
