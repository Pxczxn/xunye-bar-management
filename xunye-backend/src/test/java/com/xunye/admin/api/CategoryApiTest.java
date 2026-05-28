package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类管理 API 全面测试
 * 覆盖: CRUD、重复检查
 */
@DisplayName("API-CATEGORY: 分类管理测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryApiTest extends BaseIntegrationTest {

    private static Long createdCategoryId;

    @Test
    @Order(1)
    @DisplayName("API-CAT-001: 查询分类列表")
    void testListCategories() throws Exception {
        JsonNode resp = httpGet("/api/admin/categories", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
        assertTrue(resp.path("data").size() >= 6);
    }

    @Test
    @Order(2)
    @DisplayName("API-CAT-002: 创建分类")
    void testCreateCategory() throws Exception {
        Map<String, Object> body = Map.of("name", "测试分类", "sort", 10, "status", 1);
        JsonNode resp = httpPost("/api/admin/categories", bossToken(), body);
        assertSuccess(resp);

        // 验证创建
        JsonNode list = httpGet("/api/admin/categories", bossToken());
        boolean found = false;
        for (JsonNode item : list.path("data")) {
            if ("测试分类".equals(item.path("name").asText())) {
                createdCategoryId = item.path("id").asLong();
                found = true;
            }
        }
        assertTrue(found, "应能找到新创建的分类");
    }

    @Test
    @Order(3)
    @DisplayName("API-CAT-003: 修改分类")
    void testUpdateCategory() throws Exception {
        if (createdCategoryId == null) return;
        Map<String, Object> body = Map.of("name", "修改后分类", "sort", 20);
        JsonNode resp = httpPut("/api/admin/categories/" + createdCategoryId, bossToken(), body);
        assertSuccess(resp);
    }

    @Test
    @Order(4)
    @DisplayName("API-CAT-004: 删除分类")
    void testDeleteCategory() throws Exception {
        if (createdCategoryId == null) return;
        JsonNode resp = httpDelete("/api/admin/categories/" + createdCategoryId, bossToken());
        assertSuccess(resp);
    }

    @Test
    @Order(5)
    @DisplayName("API-CAT-005: 删除不存在的分类")
    void testDeleteNonexistent() throws Exception {
        JsonNode resp = httpDelete("/api/admin/categories/99999", bossToken());
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(10)
    @DisplayName("API-CAT-010: MANAGER可访问分类接口")
    void testManagerAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/categories", managerToken());
        assertSuccess(resp);
    }

    @Test
    @Order(11)
    @DisplayName("API-CAT-011: STAFF不可访问分类接口")
    void testStaffCannotAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/categories", staffToken());
        assertEquals(403, resp.path("code").asInt());
    }
}
