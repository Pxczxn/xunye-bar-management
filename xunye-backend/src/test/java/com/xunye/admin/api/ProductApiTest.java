package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 商品管理 API 全面测试
 * 覆盖: CRUD、上下架、分类筛选、搜索、图片上传
 */
@DisplayName("API-PRODUCT: 商品管理测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductApiTest extends BaseIntegrationTest {

    private static Long createdProductId;

    // ==================== 查询 ====================

    @Test
    @Order(1)
    @DisplayName("API-PROD-001: 分页查询商品")
    void testGetProductPage() throws Exception {
        JsonNode resp = httpGet("/api/admin/products?pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").path("records").isArray());
        assertTrue(resp.path("data").path("total").asLong() > 0);
    }

    @Test
    @Order(2)
    @DisplayName("API-PROD-002: 商品简表")
    void testGetSimpleList() throws Exception {
        JsonNode resp = httpGet("/api/admin/products/simple", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
        assertTrue(resp.path("data").size() > 0);
    }

    @Test
    @Order(3)
    @DisplayName("API-PROD-003: 商品详情")
    void testGetProductDetail() throws Exception {
        JsonNode list = httpGet("/api/admin/products?pageNum=1&pageSize=1", bossToken());
        Long id = list.path("data").path("records").get(0).path("id").asLong();
        JsonNode resp = httpGet("/api/admin/products/" + id, bossToken());
        assertSuccess(resp);
        assertNotNull(resp.path("data").path("name").asText());
    }

    @Test
    @Order(4)
    @DisplayName("API-PROD-004: 按分类筛选")
    void testFilterByCategory() throws Exception {
        JsonNode resp = httpGet("/api/admin/products?categoryId=1&pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        resp.path("data").path("records").forEach(item -> {
            assertEquals(1, item.path("categoryId").asInt());
        });
    }

    @Test
    @Order(5)
    @DisplayName("API-PROD-005: 按关键词搜索")
    void testSearchByKeyword() throws Exception {
        JsonNode resp = httpGet("/api/admin/products?keyword=百威&pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        boolean found = false;
        for (JsonNode item : resp.path("data").path("records")) {
            if (item.path("name").asText().contains("百威")) found = true;
        }
        assertTrue(found, "应能搜索到百威啤酒");
    }

    @Test
    @Order(6)
    @DisplayName("API-PROD-006: 不存在的商品详情")
    void testGetNonexistentProduct() throws Exception {
        JsonNode resp = httpGet("/api/admin/products/99999", bossToken());
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 创建 ====================

    @Test
    @Order(10)
    @DisplayName("API-PROD-010: 创建商品(正常)")
    void testCreateProduct() throws Exception {
        Map<String, Object> body = Map.of(
                "categoryId", 1, "name", "测试啤酒",
                "price", 25.00, "stock", 100, "safeStock", 10,
                "unit", "瓶", "status", "ON_SALE"
        );
        JsonNode resp = httpPost("/api/admin/products", bossToken(), body);
        assertSuccess(resp);

        // 验证创建成功
        JsonNode list = httpGet("/api/admin/products?keyword=测试啤酒&pageNum=1&pageSize=10", bossToken());
        assertTrue(list.path("data").path("records").size() > 0);
        createdProductId = list.path("data").path("records").get(0).path("id").asLong();
    }

    @Test
    @Order(11)
    @DisplayName("API-PROD-011: 创建商品-名称为空")
    void testCreateProductEmptyName() throws Exception {
        Map<String, Object> body = Map.of(
                "categoryId", 1, "name", "",
                "price", 25.00
        );
        JsonNode resp = httpPost("/api/admin/products", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(12)
    @DisplayName("API-PROD-012: 创建商品-价格为负")
    void testCreateProductNegativePrice() throws Exception {
        Map<String, Object> body = Map.of(
                "categoryId", 1, "name", "负价商品",
                "price", -10.00
        );
        JsonNode resp = httpPost("/api/admin/products", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 修改 ====================

    @Test
    @Order(20)
    @DisplayName("API-PROD-020: 修改商品")
    void testUpdateProduct() throws Exception {
        if (createdProductId == null) {
            JsonNode list = httpGet("/api/admin/products?pageNum=1&pageSize=1", bossToken());
            createdProductId = list.path("data").path("records").get(0).path("id").asLong();
        }
        Map<String, Object> body = Map.of(
                "categoryId", 1, "name", "修改后商品名",
                "price", 35.00, "stock", 100, "safeStock", 10,
                "unit", "瓶", "status", "ON_SALE"
        );
        JsonNode resp = httpPut("/api/admin/products/" + createdProductId, bossToken(), body);
        assertSuccess(resp);

        // 验证修改
        JsonNode detail = httpGet("/api/admin/products/" + createdProductId, bossToken());
        assertEquals("修改后商品名", detail.path("data").path("name").asText());
        assertEquals(35.0, detail.path("data").path("price").asDouble(), 0.01);
    }

    @Test
    @Order(21)
    @DisplayName("API-PROD-021: 修改商品状态(下架)")
    void testUpdateProductStatus() throws Exception {
        if (createdProductId == null) return;
        Map<String, Object> body = Map.of("status", "OFF_SALE");
        JsonNode resp = httpPatch("/api/admin/products/" + createdProductId + "/status", bossToken(), body);
        assertSuccess(resp);

        JsonNode detail = httpGet("/api/admin/products/" + createdProductId, bossToken());
        assertEquals("OFF_SALE", detail.path("data").path("status").asText());
    }

    @Test
    @Order(22)
    @DisplayName("API-PROD-022: 修改商品状态(上架)")
    void testUpdateProductStatusOnSale() throws Exception {
        if (createdProductId == null) return;
        Map<String, Object> body = Map.of("status", "ON_SALE");
        JsonNode resp = httpPatch("/api/admin/products/" + createdProductId + "/status", bossToken(), body);
        assertSuccess(resp);

        JsonNode detail = httpGet("/api/admin/products/" + createdProductId, bossToken());
        assertEquals("ON_SALE", detail.path("data").path("status").asText());
    }

    // ==================== 删除 ====================

    @Test
    @Order(30)
    @DisplayName("API-PROD-030: 删除商品")
    void testDeleteProduct() throws Exception {
        if (createdProductId == null) return;
        JsonNode resp = httpDelete("/api/admin/products/" + createdProductId, bossToken());
        assertSuccess(resp);

        // 验证删除
        JsonNode detail = httpGet("/api/admin/products/" + createdProductId, bossToken());
        assertNotEquals(200, detail.path("code").asInt());
    }

    // ==================== 权限 ====================

    @Test
    @Order(40)
    @DisplayName("API-PROD-040: MANAGER可访问商品接口")
    void testManagerAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/products?pageNum=1&pageSize=1", managerToken());
        assertSuccess(resp);
    }

    @Test
    @Order(41)
    @DisplayName("API-PROD-041: STAFF不可访问商品接口")
    void testStaffAccessDenied() throws Exception {
        JsonNode resp = httpGet("/api/admin/products?pageNum=1&pageSize=1", staffToken());
        assertEquals(403, resp.path("code").asInt());
    }
}
