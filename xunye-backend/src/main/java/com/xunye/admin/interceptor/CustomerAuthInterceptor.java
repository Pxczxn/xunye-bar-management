package com.xunye.admin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.annotation.CustomerAuth;
import com.xunye.admin.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class CustomerAuthInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String CUSTOMER_SESSION_PREFIX = "customer_session:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        CustomerAuth customerAuth = handlerMethod.getMethodAnnotation(CustomerAuth.class);
        if (customerAuth == null) {
            customerAuth = handlerMethod.getBeanType().getAnnotation(CustomerAuth.class);
        }

        if (customerAuth != null && customerAuth.required()) {
            String sessionId = request.getHeader("X-Customer-Session");
            if (sessionId == null || sessionId.isEmpty()) {
                writeJson(response, 401, "请先扫码进入桌台");
                return false;
            }

            String sessionKey = CUSTOMER_SESSION_PREFIX + sessionId;
            Object session = redisTemplate.opsForValue().get(sessionKey);
            if (session == null) {
                writeJson(response, 401, "会话已过期，请重新扫码");
                return false;
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
