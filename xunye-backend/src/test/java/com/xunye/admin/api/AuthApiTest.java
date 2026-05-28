package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证模块 API 全面测试
 * 覆盖: 登录、Token、权限、安全
 */
@DisplayName("API-AUTH: 认证模块测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthApiTest extends BaseIntegrationTest {

    // ==================== 登录功能 ====================

    @Test
    @Order(1)
    @DisplayName("API-AUTH-001: BOSS正确账密登录")
    void testBossLogin() throws Exception {
        JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("username", "admin", "password", "123456"));
        assertSuccess(resp);
        assertNotNull(resp.path("data").path("token").asText());
        assertEquals("BOSS", resp.path("data").path("user").path("role").asText());
        assertEquals("admin", resp.path("data").path("user").path("username").asText());
    }

    @Test
    @Order(2)
    @DisplayName("API-AUTH-002: MANAGER正确账密登录")
    void testManagerLogin() throws Exception {
        JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("username", "manager", "password", "123456"));
        assertSuccess(resp);
        assertEquals("MANAGER", resp.path("data").path("user").path("role").asText());
    }

    @Test
    @Order(3)
    @DisplayName("API-AUTH-003: STAFF正确账密登录")
    void testStaffLogin() throws Exception {
        JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("username", "staff", "password", "123456"));
        assertSuccess(resp);
        assertEquals("STAFF", resp.path("data").path("user").path("role").asText());
    }

    @Test
    @Order(4)
    @DisplayName("API-AUTH-004: 错误密码登录")
    void testWrongPassword() throws Exception {
        JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("username", "admin", "password", "wrong"));
        assertNotEquals(200, resp.path("code").asInt());
        assertTrue(resp.path("message").asText().contains("密码错误"));
    }

    @Test
    @Order(5)
    @DisplayName("API-AUTH-005: 不存在的用户登录")
    void testNonexistentUser() throws Exception {
        JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("username", "nonexist", "password", "123456"));
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(6)
    @DisplayName("API-AUTH-006: 禁用账号登录")
    void testDisabledAccount() throws Exception {
        JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("username", "disabled_user", "password", "123456"));
        assertNotEquals(200, resp.path("code").asInt());
        assertTrue(resp.path("message").asText().contains("禁用"));
    }

    @Test
    @Order(7)
    @DisplayName("API-AUTH-007: 缺少用户名")
    void testMissingUsername() throws Exception {
        JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("password", "123456"));
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(8)
    @DisplayName("API-AUTH-008: 缺少密码")
    void testMissingPassword() throws Exception {
        JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("username", "admin"));
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(9)
    @DisplayName("API-AUTH-009: 空请求体")
    void testEmptyBody() throws Exception {
        JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of());
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== Token 验证 ====================

    @Test
    @Order(10)
    @DisplayName("API-AUTH-010: 有效Token获取Profile")
    void testGetProfileValidToken() throws Exception {
        String token = bossToken();
        JsonNode resp = httpGet("/api/admin/auth/profile", token);
        assertSuccess(resp);
        assertEquals("admin", resp.path("data").path("username").asText());
    }

    @Test
    @Order(11)
    @DisplayName("API-AUTH-011: 无Token访问受保护接口")
    void testNoTokenAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/auth/profile", "");
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(12)
    @DisplayName("API-AUTH-012: 伪造Token访问")
    void testFakeTokenAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/auth/profile", "admin-token-fake1234567890abcdef12345678");
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(13)
    @DisplayName("API-AUTH-013: Token格式验证")
    void testTokenFormat() throws Exception {
        String token = loginAs("admin", "123456");
        assertNotNull(token);
        assertTrue(token.startsWith("admin-token-"), "Token应以admin-token-开头");
        assertTrue(token.length() > 30, "Token长度应>30");
    }

    @Test
    @Order(14)
    @DisplayName("API-AUTH-014: 多次登录生成不同Token")
    void testMultipleLoginDifferentTokens() throws Exception {
        String t1 = loginAs("admin", "123456");
        String t2 = loginAs("admin", "123456");
        assertNotEquals(t1, t2, "每次登录应生成不同的Token");
    }

    // ==================== 角色权限 ====================

    @Test
    @Order(15)
    @DisplayName("API-AUTH-015: STAFF访问BOSS-only接口(员工管理)")
    void testStaffAccessBossOnly() throws Exception {
        JsonNode resp = httpGet("/api/admin/staff", staffToken());
        assertEquals(403, resp.path("code").asInt());
    }

    @Test
    @Order(16)
    @DisplayName("API-AUTH-016: MANAGER访问BOSS-only接口(员工管理)")
    void testManagerAccessBossOnly() throws Exception {
        JsonNode resp = httpGet("/api/admin/staff", managerToken());
        assertEquals(403, resp.path("code").asInt());
    }

    @Test
    @Order(17)
    @DisplayName("API-AUTH-017: BOSS访问员工管理接口")
    void testBossAccessStaffManage() throws Exception {
        JsonNode resp = httpGet("/api/admin/staff", bossToken());
        assertSuccess(resp);
    }

    @Test
    @Order(18)
    @DisplayName("API-AUTH-018: MANAGER访问Dashboard接口")
    void testManagerAccessDashboard() throws Exception {
        JsonNode resp = httpGet("/api/admin/dashboard/summary", managerToken());
        assertSuccess(resp);
    }

    @Test
    @Order(19)
    @DisplayName("API-AUTH-019: STAFF访问订单接口")
    void testStaffAccessOrders() throws Exception {
        JsonNode resp = httpGet("/api/admin/orders", staffToken());
        assertSuccess(resp);
    }

    @Test
    @Order(20)
    @DisplayName("API-AUTH-020: STAFF访问桌台接口")
    void testStaffAccessTables() throws Exception {
        JsonNode resp = httpGet("/api/admin/tables", staffToken());
        assertSuccess(resp);
    }

    // ==================== 安全测试 ====================

    @Test
    @Order(21)
    @DisplayName("API-AUTH-021: SQL注入-用户名")
    void testSqlInjectionUsername() throws Exception {
        String[] payloads = {"' OR '1'='1", "admin'--", "' UNION SELECT * FROM staff_user --"};
        for (String payload : payloads) {
            JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("username", payload, "password", "x"));
            assertNotEquals(200, resp.path("code").asInt(),
                    "SQL注入 '" + payload + "' 不应成功");
        }
    }

    @Test
    @Order(22)
    @DisplayName("API-AUTH-022: XSS攻击-用户名")
    void testXssAttack() throws Exception {
        String[] payloads = {"<script>alert(1)</script>", "<img onerror=alert(1)>"};
        for (String payload : payloads) {
            JsonNode resp = httpPost("/api/admin/auth/login", "", Map.of("username", payload, "password", "x"));
            assertTrue(resp.toString().length() > 0, "XSS payload不应导致崩溃");
        }
    }

    @Test
    @Order(23)
    @DisplayName("API-AUTH-023: 响应时间<5秒")
    void testResponseTime() throws Exception {
        long start = System.currentTimeMillis();
        httpPost("/api/admin/auth/login", "", Map.of("username", "admin", "password", "123456"));
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 5000, "登录响应应<5秒，实际: " + duration + "ms");
    }

    @Test
    @Order(24)
    @DisplayName("API-AUTH-024: 登出后Token失效")
    void testLogoutInvalidatesToken() throws Exception {
        String token = loginAs("admin", "123456");
        // 登出
        httpPost("/api/admin/auth/logout", token, Map.of());
        // 再次访问
        JsonNode resp = httpGet("/api/admin/auth/profile", token);
        assertNotEquals(200, resp.path("code").asInt(), "登出后Token应失效");
    }

    @Test
    @Order(25)
    @DisplayName("API-AUTH-025: CORS头验证")
    void testCorsHeaders() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .options("/api/admin/auth/login")
                .header("Origin", "http://localhost:8847")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().exists("Access-Control-Allow-Origin"));
    }
}
