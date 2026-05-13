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
public class StaffPageBugTest {

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

    // ============ 分页边界测试 ============

    @Test
    @DisplayName("分页-pageNum=0 Bug:无边界校验,返回全部数据")
    public void testPageNumZero() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=0&pageSize=10");
        System.out.println("pageNum=0 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        int pageNum = resp.path("data").path("pageNum").asInt();
        int pageSize = resp.path("data").path("pageSize").asInt();
        int total = resp.path("data").path("total").asInt();
        int recordsCount = resp.path("data").path("records").size();
        // Bug: pageNum=0 时，后端将0传给SQL OFFSET = (0-1)*10 = -10
        // 但MySQL对负数OFFSET会忽略LIMIT，返回全部数据
        assertEquals(200, code, "当前返回200(无边界校验)");
        assertEquals(1, pageNum, "返回的pageNum被修正为1");
        assertEquals(10, pageSize, "pageSize保持10");
        assertEquals(total, recordsCount, "Bug: pageNum=0时返回了全部数据(未分页)");
    }

    @Test
    @DisplayName("分页-pageNum=-1 Bug:无边界校验,返回全部数据")
    public void testPageNumNegative() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=-1&pageSize=10");
        System.out.println("pageNum=-1 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        int total = resp.path("data").path("total").asInt();
        int recordsCount = resp.path("data").path("records").size();
        assertEquals(200, code, "当前返回200(无边界校验)");
        assertEquals(total, recordsCount, "Bug: pageNum=-1时返回了全部数据(未分页)");
    }

    @Test
    @DisplayName("分页-pageSize=0 Bug:无边界校验,返回全部数据")
    public void testPageSizeZero() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=0");
        System.out.println("pageSize=0 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        int total = resp.path("data").path("total").asInt();
        int recordsCount = resp.path("data").path("records").size();
        assertEquals(200, code, "当前返回200(无边界校验)");
        assertEquals(total, recordsCount, "Bug: pageSize=0时返回了全部数据(未分页)");
    }

    @Test
    @DisplayName("分页-pageSize=-1 Bug:无边界校验,返回全部数据")
    public void testPageSizeNegative() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=-1");
        System.out.println("pageSize=-1 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        int total = resp.path("data").path("total").asInt();
        int recordsCount = resp.path("data").path("records").size();
        assertEquals(200, code, "当前返回200(无边界校验)");
        assertEquals(total, recordsCount, "Bug: pageSize=-1时返回了全部数据(未分页)");
    }

    @Test
    @DisplayName("分页-pageNum=999999 应返回空列表(正常)")
    public void testPageNumVeryLarge() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=999999&pageSize=10");
        System.out.println("pageNum=999999 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        assertEquals(200, code, "超大pageNum应正常返回空列表");
        JsonNode records = resp.path("data").path("records");
        assertTrue(records.isArray(), "records应为数组");
        assertEquals(0, records.size(), "超大pageNum应返回空数组");
    }

    @Test
    @DisplayName("分页-pageSize=1 应返回1条(正常)")
    public void testPageSizeOne() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=1");
        System.out.println("pageSize=1 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        assertEquals(200, code, "pageSize=1应正常返回");
        JsonNode records = resp.path("data").path("records");
        assertTrue(records.isArray(), "records应为数组");
        assertTrue(records.size() <= 1, "pageSize=1最多返回1条");
    }

    @Test
    @DisplayName("分页-pageSize=1000 应正常返回(大分页)")
    public void testPageSizeLarge() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=1000");
        System.out.println("pageSize=1000 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        assertEquals(200, code, "pageSize=1000应正常返回");
    }

    @Test
    @DisplayName("分页-pageNum和pageSize都缺失(使用默认值)")
    public void testPageParamsMissing() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE);
        System.out.println("无分页参数 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        assertEquals(200, code, "无分页参数应使用默认值正常返回");
        JsonNode data = resp.path("data");
        assertTrue(data.has("records"), "应包含records字段");
        assertTrue(data.has("total"), "应包含total字段");
        assertTrue(data.has("pageNum"), "应包含pageNum字段");
        assertTrue(data.has("pageSize"), "应包含pageSize字段");
    }

    @Test
    @DisplayName("分页-pageNum为字符串'abc' 应返回错误")
    public void testPageNumString() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=abc&pageSize=10");
        System.out.println("pageNum=abc 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        String message = resp.path("message").asText();
        System.out.println("HTTP状态码: " + code + ", 消息: " + message);
        assertNotEquals(200, code, "pageNum为字符串应返回非200");
    }

    @Test
    @DisplayName("分页-pageSize为字符串'abc' 应返回错误")
    public void testPageSizeString() throws Exception {
        ensureLogin();
        JsonNode resp = getApi(BASE + "?pageNum=1&pageSize=abc");
        System.out.println("pageSize=abc 响应: " + resp.toString());
        int code = resp.path("code").asInt();
        String message = resp.path("message").asText();
        System.out.println("HTTP状态码: " + code + ", 消息: " + message);
        assertNotEquals(200, code, "pageSize为字符串应返回非200");
    }
}
