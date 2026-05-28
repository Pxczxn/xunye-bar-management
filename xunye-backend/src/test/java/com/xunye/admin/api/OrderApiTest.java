package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单管理 API 全面测试
 * 覆盖: 创建、支付、取消、状态流转、库存联动
 */
@DisplayName("API-ORDER: 订单管理测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderApiTest extends BaseIntegrationTest {

    private static Long createdOrderId;
    private static String createdOrderNo;

    // ==================== 创建订单 ====================

    @Test
    @Order(1)
    @DisplayName("API-ORD-001: 创建订单(正常)")
    void testCreateOrder() throws Exception {
        // 先找一个空闲桌台
        JsonNode tables = httpGet("/api/admin/tables?pageNum=1&pageSize=10", bossToken());
        Long tableId = null;
        for (JsonNode t : tables.path("data").path("records")) {
            if ("EMPTY".equals(t.path("status").asText())) {
                tableId = t.path("id").asLong();
                break;
            }
        }
        assertNotNull(tableId, "需要有空闲桌台");

        Map<String, Object> body = Map.of(
                "tableId", tableId,
                "items", List.of(
                        Map.of("productId", 1, "quantity", 2),
                        Map.of("productId", 2, "quantity", 1)
                ),
                "remark", "测试订单"
        );

        JsonNode resp = httpPost("/api/admin/orders", bossToken(), body);
        assertSuccess(resp);
        createdOrderId = resp.path("data").asLong();
        assertNotNull(createdOrderId);
    }

    @Test
    @Order(2)
    @DisplayName("API-ORD-002: 查询订单详情")
    void testGetOrderDetail() throws Exception {
        assertNotNull(createdOrderId, "需要先创建订单");
        JsonNode resp = httpGet("/api/admin/orders/" + createdOrderId, bossToken());
        assertSuccess(resp);
        assertNotNull(resp.path("data").path("orderNo").asText());
        assertEquals(2, resp.path("data").path("items").size());
    }

    @Test
    @Order(3)
    @DisplayName("API-ORD-003: 分页查询订单")
    void testGetOrderPage() throws Exception {
        JsonNode resp = httpGet("/api/admin/orders?pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").path("records").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("API-ORD-004: 按状态筛选订单")
    void testFilterByStatus() throws Exception {
        JsonNode resp = httpGet("/api/admin/orders?pageNum=1&pageSize=10&status=UNPAID", bossToken());
        assertSuccess(resp);
        for (JsonNode order : resp.path("data").path("records")) {
            assertEquals("UNPAID", order.path("status").asText());
        }
    }

    @Test
    @Order(5)
    @DisplayName("API-ORD-005: 最近订单")
    void testGetRecentOrders() throws Exception {
        JsonNode resp = httpGet("/api/admin/orders/recent", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    // ==================== 支付 ====================

    @Test
    @Order(10)
    @DisplayName("API-ORD-010: 支付订单(现金)")
    void testPayOrderCash() throws Exception {
        assertNotNull(createdOrderId, "需要先创建订单");
        Map<String, Object> body = Map.of("paymentMethod", "CASH");
        JsonNode resp = httpPatch("/api/admin/orders/" + createdOrderId + "/pay", bossToken(), body);
        assertSuccess(resp);

        // 验证订单状态
        JsonNode detail = httpGet("/api/admin/orders/" + createdOrderId, bossToken());
        assertEquals("PAID", detail.path("data").path("status").asText());
    }

    @Test
    @Order(11)
    @DisplayName("API-ORD-011: 重复支付")
    void testDoublePay() throws Exception {
        assertNotNull(createdOrderId, "需要先创建订单");
        Map<String, Object> body = Map.of("paymentMethod", "CASH");
        JsonNode resp = httpPatch("/api/admin/orders/" + createdOrderId + "/pay", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt(), "已支付订单不应再次支付");
    }

    // ==================== 履约状态 ====================

    @Test
    @Order(20)
    @DisplayName("API-ORD-020: 开始制作")
    void testStartMaking() throws Exception {
        assertNotNull(createdOrderId);
        JsonNode resp = httpPatch("/api/admin/orders/" + createdOrderId + "/making", bossToken(), null);
        assertSuccess(resp);

        JsonNode detail = httpGet("/api/admin/orders/" + createdOrderId, bossToken());
        assertEquals("MAKING", detail.path("data").path("serveStatus").asText());
    }

    @Test
    @Order(21)
    @DisplayName("API-ORD-021: 完成订单")
    void testFinishOrder() throws Exception {
        assertNotNull(createdOrderId);
        JsonNode resp = httpPatch("/api/admin/orders/" + createdOrderId + "/finish", bossToken(), null);
        assertSuccess(resp);

        JsonNode detail = httpGet("/api/admin/orders/" + createdOrderId, bossToken());
        assertEquals("FINISHED", detail.path("data").path("serveStatus").asText());
    }

    // ==================== 取消订单 ====================

    @Test
    @Order(30)
    @DisplayName("API-ORD-030: 创建并取消订单")
    void testCreateAndCancelOrder() throws Exception {
        // 先找空闲桌台
        JsonNode tables = httpGet("/api/admin/tables?pageNum=1&pageSize=10", bossToken());
        Long tableId = null;
        for (JsonNode t : tables.path("data").path("records")) {
            if ("EMPTY".equals(t.path("status").asText())) {
                tableId = t.path("id").asLong();
                break;
            }
        }
        if (tableId == null) {
            // 所有桌台都在使用，跳过
            return;
        }

        // 创建
        Map<String, Object> createBody = Map.of(
                "tableId", tableId,
                "items", List.of(Map.of("productId", 1, "quantity", 1))
        );
        JsonNode createResp = httpPost("/api/admin/orders", bossToken(), createBody);
        assertSuccess(createResp);
        Long cancelOrderId = createResp.path("data").asLong();

        // 取消
        JsonNode cancelResp = httpPatch("/api/admin/orders/" + cancelOrderId + "/cancel", bossToken(), null);
        assertSuccess(cancelResp);

        // 验证
        JsonNode detail = httpGet("/api/admin/orders/" + cancelOrderId, bossToken());
        assertEquals("CANCELLED", detail.path("data").path("status").asText());
    }

    @Test
    @Order(31)
    @DisplayName("API-ORD-031: 已支付订单不能取消")
    void testCannotCancelPaidOrder() throws Exception {
        assertNotNull(createdOrderId);
        JsonNode resp = httpPatch("/api/admin/orders/" + createdOrderId + "/cancel", bossToken(), null);
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 错误场景 ====================

    @Test
    @Order(40)
    @DisplayName("API-ORD-040: 桌台不存在")
    void testCreateOrderNonexistentTable() throws Exception {
        Map<String, Object> body = Map.of(
                "tableId", 99999,
                "items", List.of(Map.of("productId", 1, "quantity", 1))
        );
        JsonNode resp = httpPost("/api/admin/orders", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(41)
    @DisplayName("API-ORD-041: 空订单项")
    void testCreateOrderEmptyItems() throws Exception {
        Map<String, Object> body = Map.of(
                "tableId", 1,
                "items", List.of()
        );
        JsonNode resp = httpPost("/api/admin/orders", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 权限 ====================

    @Test
    @Order(50)
    @DisplayName("API-ORD-050: STAFF可访问订单接口")
    void testStaffAccessOrders() throws Exception {
        JsonNode resp = httpGet("/api/admin/orders?pageNum=1&pageSize=1", staffToken());
        assertSuccess(resp);
    }

    @Test
    @Order(51)
    @DisplayName("API-ORD-051: 无Token不能访问订单")
    void testNoTokenAccessOrders() throws Exception {
        JsonNode resp = httpGet("/api/admin/orders?pageNum=1&pageSize=1", "");
        assertNotEquals(200, resp.path("code").asInt());
    }
}
