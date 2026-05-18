package com.xunye.admin.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.entity.AuditLog;
import com.xunye.admin.entity.StaffUser;
import com.xunye.admin.mapper.AuditLogMapper;
import com.xunye.admin.mapper.StaffUserMapper;
import com.xunye.admin.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final StaffUserMapper staffUserMapper;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\"password\"\\s*:\\s*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECRET_PATTERN = Pattern.compile("\"(secret|apiKey|privateKey)\"\\s*:\\s*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
    private static final String MASK = "\"$1\":\"******\"";

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, com.xunye.admin.annotation.AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        AuditLog log = new AuditLog();
        log.setOperation(auditLog.operation());
        log.setModule(auditLog.module());
        log.setCreatedAt(LocalDateTime.now());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setIp(getIpAddress(request));
            log.setMethod(request.getMethod() + " " + request.getRequestURI());

            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String username = extractUsernameFromToken(token);
                log.setUsername(username);
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String params = objectMapper.writeValueAsString(Arrays.asList(args));
                params = maskSensitiveInfo(params);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                log.setParams(params);
            }
        } catch (Exception e) {
            log.setParams("参数序列化失败");
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            log.setResult("SUCCESS");
        } catch (Exception e) {
            log.setResult("FAILURE");
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500) + "...";
            }
            log.setErrorMsg(errorMsg);
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            log.setExecutionTime((int) executionTime);
            
            try {
                auditLogMapper.insert(log);
            } catch (Exception e) {
                AuditLogAspect.log.error("保存操作日志失败", e);
            }
        }

        return result;
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

    private String extractUsernameFromToken(String token) {
        try {
            Long userId = authService.getUserIdByToken(token);
            if (userId != null) {
                StaffUser user = staffUserMapper.selectById(userId);
                return user != null ? user.getUsername() : "unknown";
            }
        } catch (Exception e) {
            log.warn("提取用户名失败", e);
        }
        return "unknown";
    }

    private String maskSensitiveInfo(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        content = PASSWORD_PATTERN.matcher(content).replaceAll("\"password\":\"******\"");
        content = TOKEN_PATTERN.matcher(content).replaceAll("\"token\":\"******\"");
        content = SECRET_PATTERN.matcher(content).replaceAll(MASK);
        
        return content;
    }
}
