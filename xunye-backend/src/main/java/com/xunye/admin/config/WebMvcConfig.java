package com.xunye.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器
     * 忽略 favicon.ico 请求，避免 NoResourceFoundException
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 忽略 favicon.ico 请求
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(org.springframework.http.CacheControl.noStore());
    }

}
