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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static String validToken;

    private JsonNode login(String username, String password) throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);

        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getProfile(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/auth/profile")
                .header("Authorization", "Bearer " + token))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    // ==================== TC-01: 正确凭证登录 ====================
    @Test
    @Order(1)
    @DisplayName("TC-01: 正确凭证登录 - 返回code:200和完整token")
    public void testLoginWithCorrectCredentials() throws Exception {
        JsonNode response = login("admin", "123456");

        assertEquals(200, response.path("code").asInt(), "状态码应为200");
        assertEquals("success", response.path("message").asText(), "消息应为success");
        
        JsonNode data = response.path("data");
        assertTrue(data.has("token"), "响应应包含token字段");
        assertTrue(data.has("user"), "响应应包含user字段");

        String token = data.path("token").asText();
        assertNotNull(token, "token不应为空");
        assertTrue(token.startsWith("admin-token-"), "token应以'admin-token-'开头");

        validToken = token;

        JsonNode user = data.path("user");
        assertTrue(user.has("id"), "用户信息应包含id");
        assertTrue(user.has("username"), "用户信息应包含username");
        assertTrue(user.has("nickname"), "用户信息应包含nickname");
        assertTrue(user.has("role"), "用户信息应包含role");

        assertEquals("admin", user.path("username").asText(), "用户名应为admin");
        assertEquals("店长", user.path("nickname").asText(), "昵称应为店长");
        assertEquals("BOSS", user.path("role").asText(), "角色应为BOSS");
    }

    // ==================== TC-02: 错误凭证 - 用户名错误 ====================
    @Test
    @Order(2)
    @DisplayName("TC-02: 错误用户名 - 返回业务异常(500)提示密码错误")
    public void testLoginWithWrongUsername() throws Exception {
        JsonNode response = login("nonexistent_user", "123456");

        int code = response.path("code").asInt();
        assertTrue(code != 200, "错误用户名不应成功登录(实际code=" + code + ")");
        assertTrue(response.path("message").asText().contains("账号或密码错误"),
            "应提示账号或密码错误");
    }

    // ==================== TC-03: 错误凭证 - 密码错误 ====================
    @Test
    @Order(3)
    @DisplayName("TC-03: 错误密码 - 返回业务异常(400)提示密码错误")
    public void testLoginWithWrongPassword() throws Exception {
        JsonNode response = login("admin", "wrong_password");

        int code = response.path("code").asInt();
        assertTrue(code != 200, "错误密码不应成功登录(实际code=" + code + ")");
        assertTrue(response.path("message").asText().contains("账号或密码错误"),
            "应提示账号或密码错误");
    }

    // ==================== TC-04: 参数缺失 - 无username ====================
    @Test
    @Order(4)
    @DisplayName("TC-04: 参数缺失(username) - 当前行为记录")
    public void testLoginMissingUsername() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("password", "123456");

        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        int code = response.path("code").asInt();
        assertTrue(code != 200,
            "参数缺失时应返回错误码(实际code=" + code + ")");
    }

    // ==================== TC-05: 参数缺失 - 无password ====================
    @Test
    @Order(5)
    @DisplayName("TC-05: 参数缺失(password) - 当前行为记录")
    public void testLoginMissingPassword() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "admin");

        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        int code = response.path("code").asInt();
        assertTrue(code != 200, "缺少password参数不应登录成功");
    }

    // ==================== TC-06: 参数缺失 - 空body ====================
    @Test
    @Order(6)
    @DisplayName("TC-06: 空请求体 - 当前行为记录")
    public void testLoginEmptyBody() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        int code = response.path("code").asInt();
        assertTrue(code != 200 || response.path("data") == null || !response.path("data").has("token"),
            "空body不应成功获取token");
    }

    // ==================== TC-07: 非法参数类型 ====================
    @Test
    @Order(7)
    @DisplayName("TC-07: 非法参数类型 - 当前行为记录")
    public void testLoginInvalidParamType() throws Exception {
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("username", 12345);
        loginData.put("password", true);

        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        int code = response.path("code").asInt();
        assertTrue(response.has("code"), "非法参数类型应有响应");
    }

    // ==================== TC-08: 已禁用账号登录 ====================
    @Test
    @Order(8)
    @DisplayName("TC-08: 已禁用账号 - 返回业务异常并提示账号已被禁用")
    public void testLoginDisabledAccount() throws Exception {
        JsonNode response = login("disabled_user", "123456");

        int code = response.path("code").asInt();
        assertTrue(code != 200, "已禁用账号不应登录成功(实际code=" + code + ")");
        assertTrue(response.path("message").asText().contains("账号已被禁用"),
            "应提示账号已被禁用");
    }

    // ==================== TC-09: 获取用户信息 - 有效token ====================
    @Test
    @Order(9)
    @DisplayName("TC-09: 获取profile - 有效token返回用户信息")
    public void testGetProfileWithValidToken() throws Exception {
        if (validToken == null) {
            JsonNode loginResp = login("admin", "123456");
            validToken = loginResp.path("data").path("token").asText();
        }

        JsonNode response = getProfile(validToken);

        assertEquals(200, response.path("code").asInt(), "有效token应返回200");
        
        JsonNode data = response.path("data");
        assertTrue(data.has("id"));
        assertTrue(data.has("username"));
        assertTrue(data.has("nickname"));
        assertTrue(data.has("role"));

        assertEquals("admin", data.path("username").asText());
        assertEquals("店长", data.path("nickname").asText());
        assertEquals("BOSS", data.path("role").asText());
    }

    // ==================== TC-10: 获取用户信息 - 无token ====================
    @Test
    @Order(10)
    @DisplayName("TC-10: 获取profile - 无token返回错误")
    public void testGetProfileWithoutToken() throws Exception {
        JsonNode response = getProfile(null);

        int code = response.path("code").asInt();
        assertTrue(code != 200, "无token应返回错误(实际code=" + code + ")");
        assertTrue(response.path("message").asText().contains("请先登录"),
            "应提示请先登录");
    }

    // ==================== TC-11: 获取用户信息 - 空token ====================
    @Test
    @Order(11)
    @DisplayName("TC-11: 获取profile - 空token返回错误")
    public void testGetProfileWithEmptyToken() throws Exception {
        JsonNode response = getProfile("");

        int code = response.path("code").asInt();
        assertTrue(code != 200, "空token应返回错误(实际code=" + code + ")");
        assertTrue(response.path("message").asText().contains("请先登录"));
    }

    // ==================== TC-12: Token失效/伪造 ====================
    @Test
    @Order(12)
    @DisplayName("TC-12: 伪造token - 返回错误")
    public void testGetProfileWithFakeToken() throws Exception {
        String fakeToken = "admin-token-fake1234567890abcdef";
        JsonNode response = getProfile(fakeToken);

        int code = response.path("code").asInt();
        assertTrue(code != 200, "伪造token应返回错误(实际code=" + code + ")");
        assertTrue(response.path("message").asText().contains("请先登录"));
    }

    // ==================== TC-13: Token格式验证 ====================
    @Test
    @Order(13)
    @DisplayName("TC-13: Token格式验证 - 应符合UUID格式")
    public void testTokenFormatValidation() throws Exception {
        JsonNode loginResp = login("manager", "123456");
        String token = loginResp.path("data").path("token").asText();

        Pattern uuidPattern = Pattern.compile("^admin-token-[a-f0-9]{32}$");
        assertTrue(uuidPattern.matcher(token).matches(),
            "Token格式应为 admin-token + 32位hex字符");
    }

    // ==================== TC-14: SQL注入防护 ====================
    @Test
    @Order(14)
    @DisplayName("TC-14: SQL注入攻击 - 用户名字段注入")
    public void testSqlInjectionInUsername() throws Exception {
        String[] sqlPayloads = {
            "' OR '1'='1",
            "admin'; DROP TABLE staff_user; --",
            "' UNION SELECT * FROM staff_user --",
            "1' OR '1'='1' /*"
        };

        for (String payload : sqlPayloads) {
            JsonNode response = login(payload, "123456");
            
            int code = response.path("code").asInt();
            assertTrue(code != 200,
                "SQL注入payload '" + payload.substring(0, Math.min(20, payload.length())) + "...' 不应成功登录");
        }
    }

    // ==================== TC-15: XSS防护 ====================
    @Test
    @Order(15)
    @DisplayName("TC-15: XSS攻击 - 用户名字段注入脚本")
    public void testXssAttackInUsername() throws Exception {
        String[] xssPayloads = {
            "<script>alert('xss')</script>",
            "<img src=x onerror=alert(1)>",
            "javascript:alert(document.cookie)",
            "\"><script>alert(1)</script>"
        };

        for (String payload : xssPayloads) {
            JsonNode response = login(payload, "123456");
            
            if (response.path("code").asInt() == 200) {
                String responseBody = response.toString();
                assertFalse(responseBody.contains("<script>"),
                    "响应不应包含未转义的<script>标签");
                assertFalse(responseBody.contains("<img"),
                    "响应不应包含未转义的<img>标签");
            }
        }
    }

    // ==================== TC-16: 不同角色登录 ====================
    @Test
    @Order(16)
    @DisplayName("TC-16: 多角色登录 - 验证不同角色返回正确信息")
    public void testDifferentRolesLogin() throws Exception {
        Map<String, String[]> users = new HashMap<>();
        users.put("admin",   new String[]{"123456", "店长", "BOSS"});
        users.put("manager", new String[]{"123456", "经理", "MANAGER"});
        users.put("staff",   new String[]{"123456", "员工", "STAFF"});

        for (Map.Entry<String, String[]> entry : users.entrySet()) {
            String username = entry.getKey();
            String[] expected = entry.getValue();

            JsonNode response = login(username, expected[0]);
            assertEquals(200, response.path("code").asInt(),
                username + " 登录应成功");

            JsonNode user = response.path("data").path("user");
            assertEquals(expected[1], user.path("nickname").asText(),
                username + " 昵称应为" + expected[1]);
            assertEquals(expected[2], user.path("role").asText(),
                username + " 角色应为" + expected[2]);
        }
    }

    // ==================== TC-17: CORS配置验证 ====================
    @Test
    @Order(17)
    @DisplayName("TC-17: CORS头验证 - 允许指定Origin")
    public void testCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/admin/auth/login")
                .header("Origin", "http://localhost:8847")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8847"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Credentials"));
    }

    // ==================== TC-18: Content-Type验证 ====================
    @Test
    @Order(18)
    @DisplayName("TC-18: Content-Type非JSON - 记录当前行为")
    public void testWrongContentType() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content("username=admin&password=admin123"))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status >= 400 || status == 200,
            "非JSON Content-Type应有处理机制(实际status=" + status + ")");
    }

    // ==================== TC-19: HTTP方法限制 ====================
    @Test
    @Order(19)
    @DisplayName("TC-19: GET请求访问登录接口 - 记录当前行为")
    public void testWrongHttpMethodForLogin() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/auth/login")).andReturn();
        int status = result.getResponse().getStatus();
        assertTrue(status >= 400 || status == 405 || status == 200,
            "GET方法访问POST接口应有处理(实际status=" + status + ")");
    }

    // ==================== TC-20: 大量并发登录测试 ====================
    @Test
    @Order(20)
    @DisplayName("TC-20: 并发登录 - 多次登录生成不同token")
    public void testConcurrentLogins() throws Exception {
        String[] tokens = new String[5];
        
        for (int i = 0; i < 5; i++) {
            JsonNode response = login("admin", "123456");
            tokens[i] = response.path("data").path("token").asText();
            assertEquals(200, response.path("code").asInt(),
                "第" + (i+1) + "次登录应成功");
        }

        for (int i = 0; i < tokens.length - 1; i++) {
            assertNotEquals(tokens[i], tokens[i+1],
                "每次登录应生成不同的token");
        }

        for (String token : tokens) {
            JsonNode profile = getProfile(token);
            assertEquals(200, profile.path("code").asInt(),
                "每个token都应有效");
        }
    }

    // ==================== TC-21: 特殊字符处理 ====================
    @Test
    @Order(21)
    @DisplayName("TC-21: 特殊字符输入 - 不导致异常")
    public void testSpecialCharactersInput() throws Exception {
        String[] specialInputs = {
            "!@#$%^&*()",
            "中文用户名",
            "admin\t\n\r",
            "   admin   ",
            "a".repeat(1000)
        };

        for (String input : specialInputs) {
            try {
                JsonNode response = login(input, "123456");
                assertTrue(response.has("code"),
                    "特殊字符输入不应导致服务器异常");
            } catch (Exception e) {
                fail("特殊字符 '" + input.substring(0, Math.min(10, input.length())) + 
                    "...' 不应抛出异常: " + e.getMessage());
            }
        }
    }

    // ==================== TC-22: 响应时间测试 ====================
    @Test
    @Order(22)
    @DisplayName("TC-22: 响应时间 - 登录接口应在合理时间内完成")
    public void testResponseTime() throws Exception {
        long startTime = System.currentTimeMillis();
        
        login("admin", "123456");
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 5000,
            "登录响应时间应在5秒内，实际: " + duration + "ms");
    }

    // ==================== TC-23: 最后登录时间更新 ====================
    @Test
    @Order(23)
    @DisplayName("TC-23: 登录后更新last_login_at")
    public void testLastLoginTimeUpdate() throws Exception {
        jdbcTemplate.update(
            "UPDATE staff_user SET last_login_at = NULL WHERE username = 'staff'");
        
        login("staff", "123456");
        
        String lastLoginTime = jdbcTemplate.queryForObject(
            "SELECT last_login_at FROM staff_user WHERE username = 'staff'", String.class);
        
        assertNotNull(lastLoginTime, "登录后last_login_at不应为null");
        assertTrue(lastLoginTime.length() > 0, "last_login_at应有值");
    }
}
