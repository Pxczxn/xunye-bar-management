package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 库存管理 API 全面测试
 * 覆盖: 入库、出库、损耗、盘点、预警、流水
 */
@DisplayName("API-INVENTORY: 库存管理测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryApiTest extends BaseIntegrationTest {

    private static final Long TEST_PRODUCT_ID = 1L; // 百威啤酒

    // ==================== 入库 ====================

    @Test
    @Order(1)
    @DisplayName("API-INV-001: 入库")
    void testStockIn() throws Exception {
        // 获取当前库存
        JsonNode before = httpGet("/api/admin/products/" + TEST_PRODUCT_ID, bossToken());
        int beforeStock = before.path("data").path("stock").asInt();

        Map<String, Object> body = Map.of(
                "productId", TEST_PRODUCT_ID,
                "type", "IN",
                "quantity", 50,
                "reason", "测试入库"
        );
        JsonNode resp = httpPost("/api/admin/inventory/adjust", bossToken(), body);
        assertSuccess(resp);

        // 验证库存增加
        JsonNode after = httpGet("/api/admin/products/" + TEST_PRODUCT_ID, bossToken());
        assertEquals(beforeStock + 50, after.path("data").path("stock").asInt());
    }

    // ==================== 出库 ====================

    @Test
    @Order(2)
    @DisplayName("API-INV-002: 出库")
    void testStockOut() throws Exception {
        JsonNode before = httpGet("/api/admin/products/" + TEST_PRODUCT_ID, bossToken());
        int beforeStock = before.path("data").path("stock").asInt();

        Map<String, Object> body = Map.of(
                "productId", TEST_PRODUCT_ID,
                "type", "OUT",
                "quantity", 10,
                "reason", "测试出库"
        );
        JsonNode resp = httpPost("/api/admin/inventory/adjust", bossToken(), body);
        assertSuccess(resp);

        JsonNode after = httpGet("/api/admin/products/" + TEST_PRODUCT_ID, bossToken());
        assertEquals(beforeStock - 10, after.path("data").path("stock").asInt());
    }

    @Test
    @Order(3)
    @DisplayName("API-INV-003: 超量出库(库存不足)")
    void testOverStockOut() throws Exception {
        Map<String, Object> body = Map.of(
                "productId", TEST_PRODUCT_ID,
                "type", "OUT",
                "quantity", 99999,
                "reason", "超量测试"
        );
        JsonNode resp = httpPost("/api/admin/inventory/adjust", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 损耗 ====================

    @Test
    @Order(4)
    @DisplayName("API-INV-004: 损耗")
    void testLoss() throws Exception {
        JsonNode before = httpGet("/api/admin/products/" + TEST_PRODUCT_ID, bossToken());
        int beforeStock = before.path("data").path("stock").asInt();

        Map<String, Object> body = Map.of(
                "productId", TEST_PRODUCT_ID,
                "type", "LOSS",
                "quantity", 3,
                "reason", "破损损耗"
        );
        JsonNode resp = httpPost("/api/admin/inventory/adjust", bossToken(), body);
        assertSuccess(resp);

        JsonNode after = httpGet("/api/admin/products/" + TEST_PRODUCT_ID, bossToken());
        assertEquals(beforeStock - 3, after.path("data").path("stock").asInt());
    }

    // ==================== 盘点调整 ====================

    @Test
    @Order(5)
    @DisplayName("API-INV-005: 盘点调整")
    void testAdjust() throws Exception {
        Map<String, Object> body = Map.of(
                "productId", TEST_PRODUCT_ID,
                "type", "ADJUST",
                "quantity", 100,
                "reason", "盘点调整"
        );
        JsonNode resp = httpPost("/api/admin/inventory/adjust", bossToken(), body);
        assertSuccess(resp);

        JsonNode after = httpGet("/api/admin/products/" + TEST_PRODUCT_ID, bossToken());
        assertEquals(100, after.path("data").path("stock").asInt());
    }

    // ==================== 流水记录 ====================

    @Test
    @Order(10)
    @DisplayName("API-INV-010: 查询流水记录")
    void testGetRecords() throws Exception {
        JsonNode resp = httpGet("/api/admin/inventory/records?pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").path("records").isArray());
    }

    @Test
    @Order(11)
    @DisplayName("API-INV-011: 按商品筛选流水")
    void testGetRecordsByProduct() throws Exception {
        JsonNode resp = httpGet("/api/admin/inventory/records?pageNum=1&pageSize=10&productName=百威", bossToken());
        assertSuccess(resp);
    }

    @Test
    @Order(12)
    @DisplayName("API-INV-012: 按类型筛选流水")
    void testGetRecordsByType() throws Exception {
        JsonNode resp = httpGet("/api/admin/inventory/records?pageNum=1&pageSize=10&type=IN", bossToken());
        assertSuccess(resp);
    }

    // ==================== 预警 ====================

    @Test
    @Order(20)
    @DisplayName("API-INV-020: 库存预警")
    void testGetWarnings() throws Exception {
        JsonNode resp = httpGet("/api/admin/inventory/warnings", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    // ==================== 错误场景 ====================

    @Test
    @Order(30)
    @DisplayName("API-INV-030: 商品不存在")
    void testAdjustNonexistentProduct() throws Exception {
        Map<String, Object> body = Map.of(
                "productId", 99999,
                "type", "IN",
                "quantity", 10
        );
        JsonNode resp = httpPost("/api/admin/inventory/adjust", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(31)
    @DisplayName("API-INV-031: 无效操作类型")
    void testAdjustInvalidType() throws Exception {
        Map<String, Object> body = Map.of(
                "productId", TEST_PRODUCT_ID,
                "type", "INVALID",
                "quantity", 10
        );
        JsonNode resp = httpPost("/api/admin/inventory/adjust", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 权限 ====================

    @Test
    @Order(40)
    @DisplayName("API-INV-040: STAFF不可访问库存接口")
    void testStaffCannotAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/inventory/warnings", staffToken());
        assertEquals(403, resp.path("code").asInt());
    }

    @Test
    @Order(41)
    @DisplayName("API-INV-041: MANAGER可访问库存接口")
    void testManagerCanAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/inventory/warnings", managerToken());
        assertSuccess(resp);
    }
}
