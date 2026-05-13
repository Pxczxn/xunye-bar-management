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
public class StaffLoginFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static String adminToken;
    private static Long newStaffId;
    private String newStaffUsername = "flow_test_staff";
    private String newStaffPassword = "flowpass123";
    private String newStaffNickname = "流程测试员工";
    private static String newStaffToken;

    private static final String STAFF_BASE = "/api/admin/staff";
    private static final String AUTH_BASE = "/api/admin/auth";

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

    private JsonNode patchApi(String path, Object body, String token) throws Exception {
        var builder = patch(path).contentType(MediaType.APPLICATION_JSON);
        if (token != null) builder = builder.header("Authorization", "Bearer " + token);
        if (body != null) builder = builder.content(objectMapper.writeValueAsString(body));
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void resetData() {
        jdbcTemplate.update("DELETE FROM staff_user WHERE username = ?", newStaffUsername);
    }

    // ==================== 步骤1: 管理员登录 ====================
    @Test
    @Order(1)
    @DisplayName("步骤1: 管理员登录(admin/admin123)")
    public void testAdminLogin() throws Exception {
        resetData();
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "admin");
        loginData.put("password", "123456");
        JsonNode resp = postApi(AUTH_BASE + "/login", loginData, null);
        System.out.println("管理员登录响应: " + resp.toString());

        assertEquals(200, resp.path("code").asInt(), "管理员登录应成功");
        assertNotNull(resp.path("data").path("token").asText(), "应返回token");
        adminToken = resp.path("data").path("token").asText();

        JsonNode user = resp.path("data").path("user");
        assertEquals("admin", user.path("username").asText());
        assertEquals("店长", user.path("nickname").asText());
        assertEquals("BOSS", user.path("role").asText());
    }

    // ==================== 步骤2: 新增STAFF账号 ====================
    @Test
    @Order(2)
    @DisplayName("步骤2: 新增STAFF账号")
    public void testCreateStaff() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", newStaffUsername);
        payload.put("password", newStaffPassword);
        payload.put("nickname", newStaffNickname);
        payload.put("role", "STAFF");
        payload.put("status", "1");
        JsonNode resp = postApi(STAFF_BASE, payload, adminToken);
        System.out.println("新增员工响应: " + resp.toString());

        assertEquals(200, resp.path("code").asInt(), "新增员工应成功");

        // 验证列表中有新员工
        JsonNode page = getApi(STAFF_BASE + "?keyword=" + newStaffUsername, adminToken);
        JsonNode records = page.path("data").path("records");
        boolean found = false;
        for (JsonNode r : records) {
            if (newStaffUsername.equals(r.path("username").asText())) {
                found = true;
                newStaffId = r.path("id").asLong();
                assertEquals(newStaffNickname, r.path("nickname").asText());
                assertEquals("STAFF", r.path("role").asText());
                assertEquals(1, r.path("status").asInt());
            }
        }
        assertTrue(found, "新增后应能在列表中找到该员工");
        assertNotNull(newStaffId, "应获取到新员工的ID");
    }

    // ==================== 步骤3: 退出登录(管理员) ====================
    @Test
    @Order(3)
    @DisplayName("步骤3: 退出管理员登录")
    public void testAdminLogout() throws Exception {
        // 当前系统使用内存token存储，退出登录只需清空token
        adminToken = null;
        System.out.println("管理员token已清空，模拟退出登录");
    }

    // ==================== 步骤4: 新员工账号登录 ====================
    @Test
    @Order(4)
    @DisplayName("步骤4: 用新员工账号登录")
    public void testNewStaffLogin() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", newStaffUsername);
        loginData.put("password", newStaffPassword);
        JsonNode resp = postApi(AUTH_BASE + "/login", loginData, null);
        System.out.println("新员工登录响应: " + resp.toString());

        assertEquals(200, resp.path("code").asInt(), "新员工登录应成功");
        assertNotNull(resp.path("data").path("token").asText(), "应返回token");
        newStaffToken = resp.path("data").path("token").asText();

        JsonNode user = resp.path("data").path("user");
        assertEquals(newStaffUsername, user.path("username").asText());
        assertEquals(newStaffNickname, user.path("nickname").asText());
        assertEquals("STAFF", user.path("role").asText());
    }

    // ==================== 步骤5: 验证顶部昵称和角色显示 ====================
    @Test
    @Order(5)
    @DisplayName("步骤5: 验证profile接口返回正确的昵称和角色")
    public void testProfileInfo() throws Exception {
        JsonNode resp = getApi(AUTH_BASE + "/profile", newStaffToken);
        System.out.println("Profile接口响应: " + resp.toString());

        assertEquals(200, resp.path("code").asInt(), "获取profile应成功");
        JsonNode data = resp.path("data");
        assertEquals(newStaffNickname, data.path("nickname").asText(), "昵称应正确");
        assertEquals("STAFF", data.path("role").asText(), "角色应正确");
        assertEquals(newStaffUsername, data.path("username").asText(), "用户名应正确");
    }

    // ==================== 步骤6: 管理员重新登录并禁用该账号 ====================
    @Test
    @Order(6)
    @DisplayName("步骤6: 管理员重新登录")
    public void testAdminReLogin() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "admin");
        loginData.put("password", "123456");
        JsonNode resp = postApi(AUTH_BASE + "/login", loginData, null);
        assertEquals(200, resp.path("code").asInt(), "管理员重新登录应成功");
        adminToken = resp.path("data").path("token").asText();
    }

    @Test
    @Order(7)
    @DisplayName("步骤6: 禁用该STAFF账号")
    public void testDisableStaff() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "0");
        JsonNode resp = patchApi(STAFF_BASE + "/" + newStaffId + "/status", payload, adminToken);
        System.out.println("禁用员工响应: " + resp.toString());

        assertEquals(200, resp.path("code").asInt(), "禁用员工应成功");

        // 验证状态已变更
        JsonNode detail = getApi(STAFF_BASE + "/" + newStaffId, adminToken);
        assertEquals(0, detail.path("data").path("status").asInt(), "状态应为禁用(0)");
    }

    // ==================== 步骤7: 再登录应失败 ====================
    @Test
    @Order(8)
    @DisplayName("步骤7: 禁用后再登录应失败")
    public void testDisabledStaffLogin() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", newStaffUsername);
        loginData.put("password", newStaffPassword);
        JsonNode resp = postApi(AUTH_BASE + "/login", loginData, null);
        System.out.println("禁用后登录响应: " + resp.toString());

        assertNotEquals(200, resp.path("code").asInt(), "禁用后登录应失败");
        String message = resp.path("message").asText();
        assertTrue(message.contains("禁用") || message.contains("账号已被禁用"),
            "错误信息应提示账号被禁用: " + message);
    }

    // ==================== 步骤8: 清理数据 ====================
    @Test
    @Order(9)
    @DisplayName("步骤8: 清理测试数据")
    public void testCleanup() throws Exception {
        resetData();
        System.out.println("测试数据已清理");
    }
}
