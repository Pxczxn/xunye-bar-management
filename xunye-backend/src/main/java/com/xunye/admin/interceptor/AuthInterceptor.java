package com.xunye.admin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeJson(response, 401, "未登录，请先登录");
            return false;
        }

        String token = authHeader.substring(7);
        String errorMsg = authService.validateToken(token);
        if (errorMsg != null) {
            writeJson(response, 401, errorMsg);
            return false;
        }

        if (handler instanceof HandlerMethod handlerMethod) {
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }
            if (requireRole != null) {
                String role = authService.getRoleByToken(token);
                if (role == null || Arrays.stream(requireRole.value()).noneMatch(role::equals)) {
                    writeJson(response, 403, "无权限访问");
                    return false;
                }
            }
        }

        return true;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Object> apiResponse = ApiResponse.error(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

