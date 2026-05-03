package com.xunye.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * 允许前端 http://localhost:5173 访问后端
 */
@Configuration
public class CorsConfig {

    /**
     * 跨域过滤器配置
     */
    @Bean
    public CorsFilter corsFilter() {
        // 跨域配置
        CorsConfiguration config = new CorsConfiguration();
        // 允许的前端地址
        config.addAllowedOrigin("http://localhost:8847");
        // 允许的请求头
        config.addAllowedHeader("*");
        // 允许的请求方法
        config.addAllowedMethod("*");
        // 允许携带 Cookie
        config.setAllowCredentials(true);

        // URL 路径配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用跨域配置
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

}
