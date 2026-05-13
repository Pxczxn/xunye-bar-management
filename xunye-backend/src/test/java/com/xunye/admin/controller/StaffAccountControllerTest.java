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
public class StaffAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String token;

    private static final String BASE = "/api/admin/staff";

    private void ensureLogin() throws Exception {
        if (token == null) {
            Map<String, String> loginData = new HashMap<>();
            loginData.put("username", "admin");
            loginData.put("password", "123456");
            JsonNode resp = postApi("/api/admin/auth/login", loginData);
            assertEquals(200, resp.path("code").asInt(), "登录应成功");
            token = resp.path("data").path("token").asText();
        }
    }

    private JsonNode postApi(String path, Object body) throws Exception {
        var builder = post(path).contentType(MediaType.APPLICATION_JSON);
        if (token != null) builder = builder.header("Authorization", "Bearer " + token);
        if (body != null) builder = builder.content(objectMapper.writeValueAsString(body));
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getApi(String path) throws Exception {
        var builder = get(path);
        if (token != null) builder = builder.header("Authorization", "Bearer " + token);
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode putApi(String path, Object body) throws Exception {
        var builder = put(path).contentType(MediaType.APPLICATION_JSON);
        if (token != null) builder = builder.header("Authorization", "Bearer " + token);
        if (body != null) builder = builder.content(objectMapper.writeValueAsString(body));
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode patchApi(String path, Object body) throws Exception {
        var builder = patch(path).contentType(MediaType.APPLICATION_JSON);
        if (token != null) builder = builder.header("Authorization", "Bearer " + token);
        if (body != null) builder = builder.content(objectMapper.writeValueAsString(body));
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode deleteApi(String path) throws Exception {
        var builder = delete(path);
        if (token != null) builder = builder.header("Authorization", "Bearer " + token);
        MvcResult result = mockMvc.perform(builder).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Map<String, Object> buildStaffPayload(String username, String password, String nickname, String role, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        data.put("nickname", nickname);
        data.put("role", role);
        data.put("status", status);
        return data;
    }

    private Long createTestStaff(String suffix) throws Exception {
        Map<String, Object> payload = buildStaffPayload(
            "testuser_" + suffix, "pass123", "测试员工_" + suffix, "STAFF", "1");
        JsonNode resp = postApi(BASE, payload);
        assertEquals(200, resp.path("code").asInt(), "创建测试员工应成功: " + suffix);
        JsonNode page = getApi(BASE + "?pageNum=1&pageSize=100");
        JsonNode records = page.path("data").path("records");
        for (JsonNode r : records) {
            if ("testuser_".concat(suffix).equals(r.path("username").asText())) {
                return r.path("id").asLong();
            }
        }
        throw new RuntimeException("未找到刚创建的员工: " + suffix);
    }

    private void resetTestData() {
        jdbcTemplate.update("DELETE FROM staff_user WHERE username LIKE 'testuser_%'");
    }

    // ==================== 1. 员工列表查询 ====================

    @Test
    @Order(101)
    @DisplayName("员工列表-基本分页查询")
    public void testListBasic() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=10");
        assertEquals(200, resp.path("code").asInt());
        JsonNode data = resp.path("data");
        assertTrue(data.has("records"));
        assertTrue(data.has("total"));
        assertTrue(data.path("records").isArray());
    }

    @Test
    @Order(102)
    @DisplayName("员工列表-keyword模糊搜索")
    public void testListKeywordSearch() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("search1");

        JsonNode resp = getApi(BASE + "?keyword=search1");
        assertEquals(200, resp.path("code").asInt());
        JsonNode records = resp.path("data").path("records");
        boolean found = false;
        for (JsonNode r : records) {
            if (r.path("username").asText().contains("search1")) {
                found = true;
            }
        }
        assertTrue(found, "应能通过keyword搜到员工");

        resetTestData();
    }

    @Test
    @Order(103)
    @DisplayName("员工列表-role角色筛选")
    public void testListRoleFilter() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?role=BOSS");
        assertEquals(200, resp.path("code").asInt());
        JsonNode records = resp.path("data").path("records");
        for (JsonNode r : records) {
            assertEquals("BOSS", r.path("role").asText());
        }
    }

    @Test
    @Order(104)
    @DisplayName("员工列表-status状态筛选")
    public void testListStatusFilter() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?status=0");
        assertEquals(200, resp.path("code").asInt());
        JsonNode records = resp.path("data").path("records");
        for (JsonNode r : records) {
            assertEquals(0, r.path("status").asInt());
        }
    }

    @Test
    @Order(105)
    @DisplayName("员工列表-组合筛选keyword+role")
    public void testListCombinedFilter() throws Exception {
        ensureLogin();
        resetTestData();
        createTestStaff("combo1");

        JsonNode resp = getApi(BASE + "?keyword=combo1&role=STAFF");
        assertEquals(200, resp.path("code").asInt());
        JsonNode records = resp.path("data").path("records");
        for (JsonNode r : records) {
            assertEquals("STAFF", r.path("role").asText());
            assertTrue(r.path("username").asText().contains("combo1"));
        }

        resetTestData();
    }

    @Test
    @Order(106)
    @DisplayName("员工列表-分页边界pageNum=0(兜底为1)")
    public void testListPageNumZero() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=0&pageSize=10");
        assertEquals(200, resp.path("code").asInt(), "pageNum=0应兜底为1，返回200");
        JsonNode data = resp.path("data");
        assertEquals(1, data.path("pageNum").asInt(), "pageNum应兜底为1");
        assertEquals(10, data.path("pageSize").asInt(), "pageSize应为10");
    }

    @Test
    @Order(107)
    @DisplayName("员工列表-分页边界pageNum=-1(兜底为1)")
    public void testListPageNumNegative() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=-1&pageSize=10");
        assertEquals(200, resp.path("code").asInt(), "pageNum=-1应兜底为1，返回200");
        JsonNode data = resp.path("data");
        assertEquals(1, data.path("pageNum").asInt(), "pageNum应兜底为1");
        assertEquals(10, data.path("pageSize").asInt(), "pageSize应为10");
    }

    @Test
    @Order(108)
    @DisplayName("员工列表-分页边界pageSize=0(兜底为10)")
    public void testListPageSizeZero() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=0");
        assertEquals(200, resp.path("code").asInt(), "pageSize=0应兜底为10，返回200");
        JsonNode data = resp.path("data");
        assertEquals(1, data.path("pageNum").asInt(), "pageNum应为1");
        assertEquals(10, data.path("pageSize").asInt(), "pageSize应兜底为10");
    }

    @Test
    @Order(109)
    @DisplayName("员工列表-分页边界pageSize=-1(兜底为10)")
    public void testListPageSizeNegative() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=-1");
        assertEquals(200, resp.path("code").asInt(), "pageSize=-1应兜底为10，返回200");
        JsonNode data = resp.path("data");
        assertEquals(1, data.path("pageNum").asInt(), "pageNum应为1");
        assertEquals(10, data.path("pageSize").asInt(), "pageSize应兜底为10");
    }

    @Test
    @Order(110)
    @DisplayName("员工列表-分页边界pageSize=999(兜底为100)")
    public void testListPageSizeExceed() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=999");
        assertEquals(200, resp.path("code").asInt(), "pageSize=999应兜底为100，返回200");
        JsonNode data = resp.path("data");
        assertEquals(1, data.path("pageNum").asInt(), "pageNum应为1");
        assertEquals(100, data.path("pageSize").asInt(), "pageSize应兜底为100");
    }

    @Test
    @Order(111)
    @DisplayName("员工列表-分页边界正常参数")
    public void testListNormalParams() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=10");
        assertEquals(200, resp.path("code").asInt(), "正常参数应返回200");
        JsonNode data = resp.path("data");
        assertEquals(1, data.path("pageNum").asInt(), "pageNum应为1");
        assertEquals(10, data.path("pageSize").asInt(), "pageSize应为10");
    }

    // ==================== 2. 新增员工 ====================

    @Test
    @Order(201)
    @DisplayName("新增员工-正常创建")
    public void testCreateSuccess() throws Exception {
        ensureLogin();
        resetTestData();
        Map<String, Object> payload = buildStaffPayload("testuser_new1", "abc123", "新员工", "STAFF", "1");
        JsonNode resp = postApi(BASE, payload);
        assertEquals(200, resp.path("code").asInt(), "正常创建应返回200");

        JsonNode page = getApi(BASE + "?keyword=testuser_new1");
        JsonNode records = page.path("data").path("records");
        boolean found = false;
        for (JsonNode r : records) {
            if ("testuser_new1".equals(r.path("username").asText())) {
                found = true;
                assertEquals("新员工", r.path("nickname").asText());
                assertEquals("STAFF", r.path("role").asText());
                assertEquals(1, r.path("status").asInt());
            }
        }
        assertTrue(found, "创建后应能在列表中查到");

        resetTestData();
    }

    @Test
    @Order(202)
    @DisplayName("新增员工-用户名重复")
    public void testCreateDuplicateUsername() throws Exception {
        ensureLogin();
        resetTestData();
        createTestStaff("dup1");
        Map<String, Object> payload = buildStaffPayload("testuser_dup1", "pass123", "重复用户", "STAFF", "1");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "用户名重复应返回非200");
        assertTrue(resp.path("message").asText().contains("已存在") || resp.path("message").asText().contains("重复"),
            "错误信息应提示用户名已存在: " + resp.path("message").asText());

        resetTestData();
    }

    @Test
    @Order(203)
    @DisplayName("新增员工-用户名为空")
    public void testCreateBlankUsername() throws Exception {
        ensureLogin();
        Map<String, Object> payload = buildStaffPayload("", "pass123", "测试", "STAFF", "1");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "用户名为空应失败");
    }

    @Test
    @Order(204)
    @DisplayName("新增员工-用户名为纯空格")
    public void testCreateWhitespaceUsername() throws Exception {
        ensureLogin();
        Map<String, Object> payload = buildStaffPayload("   ", "pass123", "测试", "STAFF", "1");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "用户名为空格应失败");
    }

    @Test
    @Order(205)
    @DisplayName("新增员工-密码为空")
    public void testCreateBlankPassword() throws Exception {
        ensureLogin();
        Map<String, Object> payload = buildStaffPayload("testuser_pwd1", "", "测试", "STAFF", "1");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "密码为空应失败");
    }

    @Test
    @Order(206)
    @DisplayName("新增员工-昵称为空")
    public void testCreateBlankNickname() throws Exception {
        ensureLogin();
        Map<String, Object> payload = buildStaffPayload("testuser_nick1", "pass123", "", "STAFF", "1");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "昵称为空应失败");
    }

    @Test
    @Order(207)
    @DisplayName("新增员工-角色非法值")
    public void testCreateInvalidRole() throws Exception {
        ensureLogin();
        Map<String, Object> payload = buildStaffPayload("testuser_role1", "pass123", "测试", "ADMIN", "1");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "非法角色应失败");
    }

    @Test
    @Order(208)
    @DisplayName("新增员工-角色为小写staff")
    public void testCreateLowercaseRole() throws Exception {
        ensureLogin();
        Map<String, Object> payload = buildStaffPayload("testuser_role2", "pass123", "测试", "staff", "1");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "小写角色应失败");
    }

    @Test
    @Order(209)
    @DisplayName("新增员工-状态非法值")
    public void testCreateInvalidStatus() throws Exception {
        ensureLogin();
        Map<String, Object> payload = buildStaffPayload("testuser_stat1", "pass123", "测试", "STAFF", "2");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "非法状态值应失败");
    }

    @Test
    @Order(210)
    @DisplayName("新增员工-status为空")
    public void testCreateNullStatus() throws Exception {
        ensureLogin();
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "testuser_stat2");
        payload.put("password", "pass123");
        payload.put("nickname", "测试");
        payload.put("role", "STAFF");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "status为null应失败");
    }

    @Test
    @Order(211)
    @DisplayName("新增员工-role为空")
    public void testCreateNullRole() throws Exception {
        ensureLogin();
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "testuser_role3");
        payload.put("password", "pass123");
        payload.put("nickname", "测试");
        payload.put("status", "1");
        JsonNode resp = postApi(BASE, payload);
        assertNotEquals(200, resp.path("code").asInt(), "role为null应失败");
    }

    @Test
    @Order(212)
    @DisplayName("新增员工-所有合法角色(BOSS/MANAGER/STAFF)")
    public void testCreateAllRoles() throws Exception {
        ensureLogin();
        resetTestData();
        String[] roles = {"BOSS", "MANAGER", "STAFF"};
        for (int i = 0; i < roles.length; i++) {
            Map<String, Object> payload = buildStaffPayload("testuser_role_" + roles[i], "pass123", roles[i] + "员工", roles[i], "1");
            JsonNode resp = postApi(BASE, payload);
            assertEquals(200, resp.path("code").asInt(), "角色 " + roles[i] + " 应创建成功");
        }
        resetTestData();
    }

    // ==================== 3. 员工详情 ====================

    @Test
    @Order(301)
    @DisplayName("员工详情-查询存在的员工")
    public void testDetailSuccess() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("detail1");
        JsonNode resp = getApi(BASE + "/" + id);
        assertEquals(200, resp.path("code").asInt());
        JsonNode data = resp.path("data");
        assertEquals(id.longValue(), data.path("id").asLong());
        assertEquals("testuser_detail1", data.path("username").asText());
        assertEquals("STAFF", data.path("role").asText());
        resetTestData();
    }

    @Test
    @Order(302)
    @DisplayName("员工详情-查询不存在的员工")
    public void testDetailNotFound() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "/999999");
        assertNotEquals(200, resp.path("code").asInt(), "不存在的员工应返回非200");
        assertTrue(resp.path("message").asText().contains("不存在"), "错误信息应提示不存在: " + resp.path("message").asText());
    }

    @Test
    @Order(303)
    @DisplayName("员工详情-返回字段完整性")
    public void testDetailFields() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("detail2");
        JsonNode resp = getApi(BASE + "/" + id);
        assertEquals(200, resp.path("code").asInt());
        JsonNode data = resp.path("data");
        assertTrue(data.has("id"));
        assertTrue(data.has("username"));
        assertTrue(data.has("nickname"));
        assertTrue(data.has("role"));
        assertTrue(data.has("status"));
        assertTrue(data.has("createdAt"));
        assertFalse(data.has("password"), "详情不应返回密码字段");
        resetTestData();
    }

    // ==================== 4. 编辑员工 ====================

    @Test
    @Order(401)
    @DisplayName("编辑员工-正常修改")
    public void testUpdateSuccess() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("upd1");
        Map<String, Object> payload = new HashMap<>();
        payload.put("nickname", "修改后的昵称");
        payload.put("role", "MANAGER");
        payload.put("status", "0");
        JsonNode resp = putApi(BASE + "/" + id, payload);
        assertEquals(200, resp.path("code").asInt());

        JsonNode detail = getApi(BASE + "/" + id);
        assertEquals("修改后的昵称", detail.path("data").path("nickname").asText());
        assertEquals("MANAGER", detail.path("data").path("role").asText());
        assertEquals(0, detail.path("data").path("status").asInt());
        resetTestData();
    }

    @Test
    @Order(402)
    @DisplayName("编辑员工-不存在的员工")
    public void testUpdateNotFound() throws Exception {
        ensureLogin();
        Map<String, Object> payload = new HashMap<>();
        payload.put("nickname", "修改");
        payload.put("role", "STAFF");
        payload.put("status", "1");
        JsonNode resp = putApi(BASE + "/999999", payload);
        assertNotEquals(200, resp.path("code").asInt());
        assertTrue(resp.path("message").asText().contains("不存在"));
    }

    @Test
    @Order(403)
    @DisplayName("编辑员工-nickname为空")
    public void testUpdateBlankNickname() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("upd2");
        Map<String, Object> payload = new HashMap<>();
        payload.put("nickname", "");
        payload.put("role", "STAFF");
        payload.put("status", "1");
        JsonNode resp = putApi(BASE + "/" + id, payload);
        assertNotEquals(200, resp.path("code").asInt(), "昵称为空应失败");
        resetTestData();
    }

    @Test
    @Order(404)
    @DisplayName("编辑员工-role非法值")
    public void testUpdateInvalidRole() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("upd3");
        Map<String, Object> payload = new HashMap<>();
        payload.put("nickname", "测试");
        payload.put("role", "INVALID");
        payload.put("status", "1");
        JsonNode resp = putApi(BASE + "/" + id, payload);
        assertNotEquals(200, resp.path("code").asInt(), "非法角色应失败");
        resetTestData();
    }

    @Test
    @Order(405)
    @DisplayName("编辑员工-不修改username/password")
    public void testUpdateDoesNotChangeUsername() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("upd4");
        Map<String, Object> payload = new HashMap<>();
        payload.put("nickname", "新昵称");
        payload.put("role", "BOSS");
        payload.put("status", "1");
        putApi(BASE + "/" + id, payload);

        JsonNode detail = getApi(BASE + "/" + id);
        assertEquals("testuser_upd4", detail.path("data").path("username").asText(), "编辑不应改变username");
        resetTestData();
    }

    // ==================== 5. 员工状态切换 ====================

    @Test
    @Order(501)
    @DisplayName("状态切换-启用到禁用")
    public void testStatusToggleDisable() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("stat1");
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "0");
        JsonNode resp = patchApi(BASE + "/" + id + "/status", payload);
        assertEquals(200, resp.path("code").asInt());

        JsonNode detail = getApi(BASE + "/" + id);
        assertEquals(0, detail.path("data").path("status").asInt());
        resetTestData();
    }

    @Test
    @Order(502)
    @DisplayName("状态切换-禁用到启用")
    public void testStatusToggleEnable() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("stat2");
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("status", "0");
        patchApi(BASE + "/" + id + "/status", payload1);

        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("status", "1");
        JsonNode resp = patchApi(BASE + "/" + id + "/status", payload2);
        assertEquals(200, resp.path("code").asInt());

        JsonNode detail = getApi(BASE + "/" + id);
        assertEquals(1, detail.path("data").path("status").asInt());
        resetTestData();
    }

    @Test
    @Order(503)
    @DisplayName("状态切换-不存在的员工")
    public void testStatusToggleNotFound() throws Exception {
        ensureLogin();
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "0");
        JsonNode resp = patchApi(BASE + "/999999/status", payload);
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(504)
    @DisplayName("状态切换-status非法值")
    public void testStatusToggleInvalidValue() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("stat3");
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "2");
        JsonNode resp = patchApi(BASE + "/" + id + "/status", payload);
        assertNotEquals(200, resp.path("code").asInt(), "非法状态值应失败");
        resetTestData();
    }

    @Test
    @Order(505)
    @DisplayName("状态切换-status为空")
    public void testStatusToggleNull() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("stat4");
        Map<String, Object> payload = new HashMap<>();
        JsonNode resp = patchApi(BASE + "/" + id + "/status", payload);
        assertNotEquals(200, resp.path("code").asInt(), "status为null应失败");
        resetTestData();
    }

    // ==================== 6. 重置密码 ====================

    @Test
    @Order(601)
    @DisplayName("重置密码-正常重置")
    public void testResetPasswordSuccess() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("pwd1");
        Map<String, Object> payload = new HashMap<>();
        payload.put("password", "newpass456");
        JsonNode resp = patchApi(BASE + "/" + id + "/reset-password", payload);
        assertEquals(200, resp.path("code").asInt());
        resetTestData();
    }

    @Test
    @Order(602)
    @DisplayName("重置密码-不存在的员工")
    public void testResetPasswordNotFound() throws Exception {
        ensureLogin();
        Map<String, Object> payload = new HashMap<>();
        payload.put("password", "newpass");
        JsonNode resp = patchApi(BASE + "/999999/reset-password", payload);
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(603)
    @DisplayName("重置密码-密码为空")
    public void testResetPasswordBlank() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("pwd2");
        Map<String, Object> payload = new HashMap<>();
        payload.put("password", "");
        JsonNode resp = patchApi(BASE + "/" + id + "/reset-password", payload);
        assertNotEquals(200, resp.path("code").asInt(), "密码为空应失败");
        resetTestData();
    }

    @Test
    @Order(604)
    @DisplayName("重置密码-重置后用新密码登录")
    public void testResetPasswordThenLogin() throws Exception {
        ensureLogin();
        resetTestData();
        Map<String, Object> createPayload = buildStaffPayload("testuser_pwdlogin", "oldpass123", "密码登录测试", "STAFF", "1");
        postApi(BASE, createPayload);
        JsonNode page = getApi(BASE + "?keyword=testuser_pwdlogin");
        Long id = page.path("data").path("records").get(0).path("id").asLong();

        Map<String, Object> resetPayload = new HashMap<>();
        resetPayload.put("password", "brandnew789");
        JsonNode resetResp = patchApi(BASE + "/" + id + "/reset-password", resetPayload);
        assertEquals(200, resetResp.path("code").asInt());

        token = null;
        Map<String, String> loginWithNew = new HashMap<>();
        loginWithNew.put("username", "testuser_pwdlogin");
        loginWithNew.put("password", "brandnew789");
        JsonNode loginResp = postApi("/api/admin/auth/login", loginWithNew);
        assertEquals(200, loginResp.path("code").asInt(), "重置密码后应能用新密码登录");

        token = null;
        Map<String, String> loginWithOld = new HashMap<>();
        loginWithOld.put("username", "testuser_pwdlogin");
        loginWithOld.put("password", "oldpass123");
        JsonNode oldResp = postApi("/api/admin/auth/login", loginWithOld);
        assertNotEquals(200, oldResp.path("code").asInt(), "重置密码后旧密码应不能登录");

        ensureLogin();
        resetTestData();
    }

    // ==================== 7. 删除员工(软删除) ====================

    @Test
    @Order(701)
    @DisplayName("删除员工-正常删除")
    public void testDeleteSuccess() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("del1");
        JsonNode resp = deleteApi(BASE + "/" + id);
        assertEquals(200, resp.path("code").asInt());
        resetTestData();
    }

    @Test
    @Order(702)
    @DisplayName("删除员工-删除后不在列表中")
    public void testDeleteNotInList() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("del2");
        deleteApi(BASE + "/" + id);

        JsonNode page = getApi(BASE + "?keyword=testuser_del2");
        JsonNode records = page.path("data").path("records");
        boolean found = false;
        for (JsonNode r : records) {
            if ("testuser_del2".equals(r.path("username").asText())) {
                found = true;
            }
        }
        assertFalse(found, "删除后不应在列表中出现");

        resetTestData();
    }

    @Test
    @Order(703)
    @DisplayName("删除员工-软删除验证(数据库仍有记录)")
    public void testDeleteSoftDelete() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("del3");
        deleteApi(BASE + "/" + id);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM staff_user WHERE id = ?", id);
        assertFalse(rows.isEmpty(), "软删除后数据库应仍有记录(deleted=1)");
        assertEquals(1, ((Number) rows.get(0).get("deleted")).intValue(), "deleted字段应为1");

        resetTestData();
    }

    @Test
    @Order(704)
    @DisplayName("删除员工-删除后详情查询应不存在")
    public void testDeleteThenDetail() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("del4");
        deleteApi(BASE + "/" + id);

        JsonNode resp = getApi(BASE + "/" + id);
        assertNotEquals(200, resp.path("code").asInt(), "删除后查询详情应返回非200");

        resetTestData();
    }

    @Test
    @Order(705)
    @DisplayName("删除员工-不存在的员工")
    public void testDeleteNotFound() throws Exception {
        ensureLogin();
        JsonNode resp = deleteApi(BASE + "/999999");
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(706)
    @DisplayName("删除员工-重复删除同一员工")
    public void testDeleteDuplicate() throws Exception {
        ensureLogin();
        resetTestData();
        Long id = createTestStaff("del5");
        deleteApi(BASE + "/" + id);
        JsonNode resp = deleteApi(BASE + "/" + id);
        assertNotEquals(200, resp.path("code").asInt(), "重复删除应返回非200");
        resetTestData();
    }
}
