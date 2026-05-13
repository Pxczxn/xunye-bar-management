package com.xunye.admin.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenAuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static String validToken;
    private static String fakeToken = "admin-token-fake123456789";

    private static final String AUTH_BASE = "/api/admin/auth";
    private static final String DASHBOARD_BASE = "/api/admin/dashboard";

    private JsonNode postApi(String path, Object body, String token) throws Exception {
        var builder = post(path).contentType(MediaType.APPLICATION_JSON);
        if (token != null) builder = builder.header("Authorization", "Bearer " + token);
        if (body != null) builder = builder.content(objectMapper.writeValueAsString(body));
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getApi(String path, String token) throws Exception {
        var builder = get(path);
        if (token != null) builder = builder.header("Authorization", "Bearer " + token);
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    // ==================== 场景1: 正常登录后访问Dashboard ====================
    @Test
    @Order(1)
    @DisplayName("场景1: 正常登录后访问Dashboard")
    public void testNormalLoginAndAccessDashboard() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "admin");
        loginData.put("password", "123456");
        JsonNode resp = postApi(AUTH_BASE + "/login", loginData, null);
        System.out.println("登录响应: " + resp.toString());

        assertEquals(200, resp.path("code").asInt(), "登录应成功");
        validToken = resp.path("data").path("token").asText();
        assertNotNull(validToken, "应返回有效token");

        // 访问Dashboard
        JsonNode dashboard = getApi(DASHBOARD_BASE + "/summary", validToken);
        System.out.println("Dashboard响应: " + dashboard.toString());
        assertEquals(200, dashboard.path("code").asInt(), "Dashboard应正常访问");
        assertTrue(dashboard.path("data").has("todayRevenue"), "应包含todayRevenue字段");
    }

    // ==================== 场景2: 清空token后访问(模拟localStorage被清除) ====================
    @Test
    @Order(2)
    @DisplayName("场景2: 无token访问业务接口(模拟清空token)")
    public void testAccessWithoutToken() throws Exception {
        JsonNode resp = getApi(DASHBOARD_BASE + "/summary", null);
        System.out.println("无token访问响应: " + resp.toString());

        assertNotEquals(200, resp.path("code").asInt(), "无token应返回非200");
        String message = resp.path("message").asText();
        assertTrue(message.contains("登录") || message.contains("请先登录") || message.contains("token"),
            "应提示登录相关错误: " + message);
    }

    // ==================== 场景3: 修改token为错误值 ====================
    @Test
    @Order(3)
    @DisplayName("场景3: 使用假token访问业务接口")
    public void testAccessWithFakeToken() throws Exception {
        JsonNode resp = getApi(DASHBOARD_BASE + "/summary", fakeToken);
        System.out.println("假token访问响应: " + resp.toString());

        assertNotEquals(200, resp.path("code").asInt(), "假token应返回非200");
        String message = resp.path("message").asText();
        assertTrue(message.contains("登录") || message.contains("请先登录") || message.contains("token"),
            "应提示登录相关错误: " + message);
    }

    // ==================== 场景4: Token过期(后端ConcurrentHashMap重启后失效) ====================
    @Test
    @Order(4)
    @DisplayName("场景4: 模拟token过期(使用不存在的token)")
    public void testExpiredToken() throws Exception {
        String expiredToken = "admin-token-expired000000000000000";
        JsonNode resp = getApi(DASHBOARD_BASE + "/summary", expiredToken);
        System.out.println("过期token访问响应: " + resp.toString());

        assertNotEquals(200, resp.path("code").asInt(), "过期token应返回非200");
        String message = resp.path("message").asText();
        assertTrue(message.contains("登录") || message.contains("请先登录") || message.contains("token"),
            "应提示登录相关错误: " + message);
    }

    // ==================== 场景5: /login页面不重复处理401 ====================
    @Test
    @Order(5)
    @DisplayName("场景5: 登录接口本身不需要token")
    public void testLoginPageNoTokenRequired() throws Exception {
        // 登录接口本身不需要token，直接访问应正常处理
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "wronguser");
        loginData.put("password", "wrongpass");
        JsonNode resp = postApi(AUTH_BASE + "/login", loginData, null);
        System.out.println("错误凭据登录响应: " + resp.toString());

        assertNotEquals(200, resp.path("code").asInt(), "错误凭据应登录失败");
        String message = resp.path("message").asText();
        assertTrue(message.contains("用户名或密码错误") || message.contains("账号或密码错误"),
            "应提示用户名或密码错误: " + message);
    }

    // ==================== 场景6: 批量请求均返回401(仅跳转一次) ====================
    @Test
    @Order(6)
    @DisplayName("场景6: 多个无token请求均返回401")
    public void testMultipleUnauthorizedRequests() throws Exception {
        String[] paths = {
            DASHBOARD_BASE + "/summary",
            DASHBOARD_BASE + "/trend",
            DASHBOARD_BASE + "/payment-methods",
            DASHBOARD_BASE + "/hot-products"
        };

        int failCount = 0;
        for (String path : paths) {
            JsonNode resp = getApi(path, null);
            int code = resp.path("code").asInt();
            if (code != 200) {
                failCount++;
            }
        }

        System.out.println("4个无token请求中，" + failCount + "个返回非200");
        assertEquals(4, failCount, "所有无token请求都应返回非200");
    }

    // ==================== 场景7: 验证profile接口token校验 ====================
    @Test
    @Order(7)
    @DisplayName("场景7: profile接口token校验")
    public void testProfileWithVariousTokens() throws Exception {
        // 有效token
        JsonNode resp1 = getApi(AUTH_BASE + "/profile", validToken);
        assertEquals(200, resp1.path("code").asInt(), "有效token应返回200");

        // 无token
        JsonNode resp2 = getApi(AUTH_BASE + "/profile", null);
        assertNotEquals(200, resp2.path("code").asInt(), "无token应返回非200");

        // 假token
        JsonNode resp3 = getApi(AUTH_BASE + "/profile", fakeToken);
        assertNotEquals(200, resp3.path("code").asInt(), "假token应返回非200");
    }

    // ==================== 场景8: 验证staff接口token校验 ====================
    @Test
    @Order(8)
    @DisplayName("场景8: staff接口token校验")
    public void testStaffWithVariousTokens() throws Exception {
        // 有效token
        JsonNode resp1 = getApi("/api/admin/staff?pageNum=1&pageSize=10", validToken);
        assertEquals(200, resp1.path("code").asInt(), "有效token应返回200");

        // 无token
        JsonNode resp2 = getApi("/api/admin/staff?pageNum=1&pageSize=10", null);
        assertNotEquals(200, resp2.path("code").asInt(), "无token应返回非200");

        // 假token
        JsonNode resp3 = getApi("/api/admin/staff?pageNum=1&pageSize=10", fakeToken);
        assertNotEquals(200, resp3.path("code").asInt(), "假token应返回非200");
    }
}
