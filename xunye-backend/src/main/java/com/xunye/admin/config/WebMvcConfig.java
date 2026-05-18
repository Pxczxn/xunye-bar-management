package com.xunye.admin.config;

import com.xunye.admin.interceptor.AuthInterceptor;
import com.xunye.admin.interceptor.CustomerAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final CustomerAuthInterceptor customerAuthInterceptor;

    @Value("${file.upload.base-path}")
    private String fileUploadBasePath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");

        registry.addInterceptor(customerAuthInterceptor)
                .addPathPatterns("/api/customer/**")
                .excludePathPatterns(
                        "/api/customer/shop/info",
                        "/api/customer/tables",
                        "/api/customer/tables/*",
                        "/api/customer/categories",
                        "/api/customer/products",
                        "/api/customer/products/*"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置图片静态资源访问
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + fileUploadBasePath + "/");
    }
}
