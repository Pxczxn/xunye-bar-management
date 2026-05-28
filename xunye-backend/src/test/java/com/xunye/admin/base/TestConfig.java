package com.xunye.admin.base;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 测试配置 - 排除限流切面，避免测试时触发429
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(
    basePackages = "com.xunye.admin",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.xunye.admin.aspect.RateLimitAspect.class,
            com.xunye.admin.aspect.AuditLogAspect.class
        }
    )
)
public class TestConfig {
}
