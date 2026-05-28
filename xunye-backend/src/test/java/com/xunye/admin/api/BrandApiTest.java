package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 品牌管理 API 全面测试
 * 覆盖: 查询、创建、删除
 */
@DisplayName("API-BRAND: 品牌管理测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BrandApiTest extends BaseIntegrationTest {

    private static Long createdBrandId;

    @Test
    @Order(1)
    @DisplayName("API-BRAND-001: 查询品牌列表")
    void testListBrands() throws Exception {
        JsonNode resp = httpGet("/api/admin/brands", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    @Test
    @Order(2)
    @DisplayName("API-BRAND-002: 创建品牌")
    void testCreateBrand() throws Exception {
        String brandName = "测试品牌_" + System.currentTimeMillis();
        Map<String, Object> body = Map.of("name", brandName, "sort", 1);
        JsonNode resp = httpPost("/api/admin/brands", bossToken(), body);
        assertSuccess(resp);
        assertNotNull(resp.path("data").path("id").asLong());

        // 找到创建的品牌
        JsonNode list = httpGet("/api/admin/brands", bossToken());
        for (JsonNode item : list.path("data")) {
            if (item.path("name").asText().startsWith("测试品牌_")) {
                createdBrandId = item.path("id").asLong();
                break;
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("API-BRAND-004: 删除品牌")
    void testDeleteBrand() throws Exception {
        if (createdBrandId == null) return;
        JsonNode resp = httpDelete("/api/admin/brands/" + createdBrandId, bossToken());
        assertSuccess(resp);
    }

    @Test
    @Order(5)
    @DisplayName("API-BRAND-005: 删除不存在的品牌")
    void testDeleteNonexistent() throws Exception {
        JsonNode resp = httpDelete("/api/admin/brands/99999", bossToken());
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(10)
    @DisplayName("API-BRAND-010: STAFF不可访问品牌接口")
    void testStaffCannotAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/brands", staffToken());
        assertEquals(403, resp.path("code").asInt());
    }
}
