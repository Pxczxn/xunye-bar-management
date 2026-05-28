package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 仪表盘 API 全面测试
 * 覆盖: 概览、销售趋势、热销商品、支付方式
 */
@DisplayName("API-DASHBOARD: 仪表盘测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardApiTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    @DisplayName("API-DASH-001: 获取概览数据")
    void testGetSummary() throws Exception {
        JsonNode resp = httpGet("/api/admin/dashboard/summary", bossToken());
        assertSuccess(resp);
        assertNotNull(resp.path("data"));
    }

    @Test
    @Order(2)
    @DisplayName("API-DASH-002: 获取销售趋势")
    void testGetSalesTrend() throws Exception {
        JsonNode resp = httpGet("/api/admin/dashboard/sales-trend", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("API-DASH-003: 获取热销商品")
    void testGetHotProducts() throws Exception {
        JsonNode resp = httpGet("/api/admin/dashboard/hot-products", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("API-DASH-004: 获取支付方式统计")
    void testGetPaymentMethods() throws Exception {
        JsonNode resp = httpGet("/api/admin/dashboard/payment-methods", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    @Test
    @Order(10)
    @DisplayName("API-DASH-010: MANAGER可访问Dashboard")
    void testManagerAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/dashboard/summary", managerToken());
        assertSuccess(resp);
    }

    @Test
    @Order(11)
    @DisplayName("API-DASH-011: STAFF不可访问Dashboard")
    void testStaffCannotAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/dashboard/summary", staffToken());
        assertEquals(403, resp.path("code").asInt());
    }
}
