package com.xunye.admin.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 集成测试基类
 * 提供登录、请求工具方法，所有测试共享一个 token 以避免限流
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected static String bossToken;
    protected static String managerToken;
    protected static String staffToken;

    // ==================== 登录工具 ====================

    protected String loginAs(String username, String password) throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);

        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("data").path("token").asText();
    }

    protected String bossToken() throws Exception {
        if (bossToken == null || bossToken.isEmpty()) {
            bossToken = loginAs("admin", "123456");
        }
        return bossToken;
    }

    protected String managerToken() throws Exception {
        if (managerToken == null || managerToken.isEmpty()) {
            managerToken = loginAs("manager", "123456");
        }
        return managerToken;
    }

    protected String staffToken() throws Exception {
        if (staffToken == null || staffToken.isEmpty()) {
            staffToken = loginAs("staff", "123456");
        }
        return staffToken;
    }

    // ==================== 请求工具 ====================

    protected JsonNode httpGet(String url, String token) throws Exception {
        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected JsonNode httpPost(String url, String token, Object body) throws Exception {
        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected JsonNode httpPut(String url, String token, Object body) throws Exception {
        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected JsonNode httpDelete(String url, String token) throws Exception {
        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected JsonNode httpPatch(String url, String token, Object body) throws Exception {
        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body != null ? objectMapper.writeValueAsString(body) : ""))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    // ==================== 断言工具 ====================

    protected void assertSuccess(JsonNode response) {
        Assertions.assertEquals(200, response.path("code").asInt(),
                "API 应返回 code=200, 实际: " + response.path("message").asText());
    }

    protected void assertError(JsonNode response, int expectedCode) {
        Assertions.assertNotEquals(200, response.path("code").asInt(),
                "API 应返回错误码, 实际返回了 200");
    }

    protected void assertHasField(JsonNode response, String field) {
        Assertions.assertTrue(response.path("data").has(field),
                "响应 data 应包含字段: " + field);
    }
}
