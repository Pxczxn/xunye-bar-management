package com.xunye.admin.aspect;

import com.xunye.admin.annotation.RateLimited;
import com.xunye.admin.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Around("@annotation(rateLimited)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = getIpAddress(request);
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        
        String key = rateLimited.key();
        if (key.isEmpty()) {
            key = methodName;
        }
        
        String redisKey = RATE_LIMIT_PREFIX + key + ":" + ip;
        
        Integer count = (Integer) redisTemplate.opsForValue().get(redisKey);
        if (count == null) {
            redisTemplate.opsForValue().set(redisKey, 1, rateLimited.period(), TimeUnit.SECONDS);
        } else if (count >= rateLimited.limit()) {
            log.warn("限流触发: IP={}, 接口={}, 限制={}/{}秒", ip, key, rateLimited.limit(), rateLimited.period());
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        } else {
            redisTemplate.opsForValue().increment(redisKey);
        }

        return joinPoint.proceed();
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
