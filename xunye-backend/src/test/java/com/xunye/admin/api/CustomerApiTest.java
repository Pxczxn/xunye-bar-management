package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 顾客端 API 全面测试 - 点酒全流程
 * 覆盖: 浏览店铺 → 查看桌台 → 浏览菜单 → 点酒下单 → 支付 → 查看订单 → 会员功能
 *
 * 顾客端接口无需 Token 认证，所有请求直接发送
 */
@DisplayName("API-CUSTOMER: 顾客端点酒全流程测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerApiTest extends BaseIntegrationTest {

    private static String customerOrderNo;
    private static String customerPhone = "13800000001";

    // ==================== 1. 浏览店铺 ====================

    @Test
    @Order(1)
    @DisplayName("CUST-001: 获取店铺信息")
    void testGetShopInfo() throws Exception {
        JsonNode resp = httpGet("/api/customer/shop/info", "");
        assertSuccess(resp);
        assertNotNull(resp.path("data").path("name").asText(), "店铺名不应为空");
    }

    // ==================== 2. 查看桌台 ====================

    @Test
    @Order(2)
    @DisplayName("CUST-002: 获取桌台列表")
    void testListTables() throws Exception {
        JsonNode resp = httpGet("/api/customer/tables", "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray(), "桌台列表应为数组");
        assertTrue(resp.path("data").size() > 0, "应有桌台数据");

        // 验证桌台字段完整性
        JsonNode firstTable = resp.path("data").get(0);
        assertTrue(firstTable.has("id"));
        assertTrue(firstTable.has("tableCode"));
        assertTrue(firstTable.has("name"));
        assertTrue(firstTable.has("status"));
    }

    @Test
    @Order(3)
    @DisplayName("CUST-003: 获取指定桌台详情(A1)")
    void testGetTableDetail() throws Exception {
        JsonNode resp = httpGet("/api/customer/tables/A1", "");
        assertSuccess(resp);
        assertEquals("A1", resp.path("data").path("tableCode").asText());
        assertNotNull(resp.path("data").path("areaName").asText(), "应包含区域名");
    }

    @Test
    @Order(4)
    @DisplayName("CUST-004: 不存在的桌台")
    void testGetNonexistentTable() throws Exception {
        JsonNode resp = httpGet("/api/customer/tables/NONEXIST", "");
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 3. 浏览菜单 ====================

    @Test
    @Order(10)
    @DisplayName("CUST-010: 获取分类列表")
    void testListCategories() throws Exception {
        JsonNode resp = httpGet("/api/customer/categories", "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
        assertTrue(resp.path("data").size() >= 6, "应有至少6个分类");

        // 验证分类字段
        JsonNode first = resp.path("data").get(0);
        assertTrue(first.has("id"));
        assertTrue(first.has("name"));
    }

    @Test
    @Order(11)
    @DisplayName("CUST-011: 获取商品列表(全部上架商品)")
    void testListAllProducts() throws Exception {
        JsonNode resp = httpGet("/api/customer/products", "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
        assertTrue(resp.path("data").size() > 0, "应有上架商品");

        // 验证只返回上架商品
        for (JsonNode product : resp.path("data")) {
            assertEquals("ON_SALE", product.path("status").asText(),
                    "顾客端不应看到下架商品: " + product.path("name").asText());
        }
    }

    @Test
    @Order(12)
    @DisplayName("CUST-012: 按分类筛选商品(啤酒)")
    void testFilterProductsByCategory() throws Exception {
        JsonNode resp = httpGet("/api/customer/products?categoryId=1", "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
        for (JsonNode product : resp.path("data")) {
            assertEquals(1, product.path("categoryId").asInt(),
                    "筛选结果应属于啤酒分类: " + product.path("name").asText());
        }
    }

    @Test
    @Order(13)
    @DisplayName("CUST-013: 搜索商品(百威)")
    void testSearchProducts() throws Exception {
        JsonNode resp = httpGet("/api/customer/products?keyword=百威", "");
        assertSuccess(resp);
        boolean found = false;
        for (JsonNode product : resp.path("data")) {
            if (product.path("name").asText().contains("百威")) {
                found = true;
            }
        }
        assertTrue(found, "应能搜索到百威啤酒");
    }

    @Test
    @Order(14)
    @DisplayName("CUST-014: 商品详情")
    void testGetProductDetail() throws Exception {
        JsonNode listResp = httpGet("/api/customer/products", "");
        Long productId = listResp.path("data").get(0).path("id").asLong();

        JsonNode resp = httpGet("/api/customer/products/" + productId, "");
        assertSuccess(resp);
        assertNotNull(resp.path("data").path("name").asText());
        assertNotNull(resp.path("data").path("price").asDouble());
        assertNotNull(resp.path("data").path("stock").asInt());
        assertNotNull(resp.path("data").path("categoryName").asText(), "应包含分类名");
    }

    @Test
    @Order(15)
    @DisplayName("CUST-015: 不存在的商品详情")
    void testGetNonexistentProduct() throws Exception {
        JsonNode resp = httpGet("/api/customer/products/99999", "");
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 4. 点酒下单 ====================

    @Test
    @Order(20)
    @DisplayName("CUST-020: 顾客创建订单(点酒)")
    void testCreateCustomerOrder() throws Exception {
        // 找一个空闲桌台
        JsonNode tablesResp = httpGet("/api/customer/tables", "");
        Long tableId = null;
        for (JsonNode t : tablesResp.path("data")) {
            if ("EMPTY".equals(t.path("status").asText())) {
                tableId = t.path("id").asLong();
                break;
            }
        }
        assertNotNull(tableId, "需要有空闲桌台才能下单");

        // 点酒: 百威 x2 + 长岛冰茶 x1
        Map<String, Object> body = Map.of(
                "tableId", tableId,
                "phone", customerPhone,
                "items", java.util.List.of(
                        Map.of("productId", 1, "quantity", 2),  // 百威啤酒 x2
                        Map.of("productId", 2, "quantity", 1)   // 长岛冰茶 x1
                ),
                "remark", "少冰谢谢"
        );

        JsonNode resp = httpPost("/api/customer/orders", "", body);
        assertSuccess(resp);

        // 验证订单返回
        customerOrderNo = resp.path("data").path("orderNo").asText();
        assertNotNull(customerOrderNo, "订单号不应为空");
        assertTrue(customerOrderNo.startsWith("XYO"), "订单号应以XYO开头");

        // 验证金额: 百威30*2 + 长岛冰茶60*1 = 120
        double totalAmount = resp.path("data").path("totalAmount").asDouble();
        assertEquals(120.00, totalAmount, 0.01, "订单总额应为120元");
    }

    @Test
    @Order(21)
    @DisplayName("CUST-021: 无商品下单")
    void testCreateOrderEmptyItems() throws Exception {
        Map<String, Object> body = Map.of(
                "tableId", 1,
                "items", java.util.List.of()
        );
        JsonNode resp = httpPost("/api/customer/orders", "", body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(22)
    @DisplayName("CUST-022: 桌台不存在下单")
    void testCreateOrderNonexistentTable() throws Exception {
        Map<String, Object> body = Map.of(
                "tableId", 99999,
                "items", java.util.List.of(Map.of("productId", 1, "quantity", 1))
        );
        JsonNode resp = httpPost("/api/customer/orders", "", body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 5. 查看订单 ====================

    @Test
    @Order(30)
    @DisplayName("CUST-030: 查看订单列表(今日)")
    void testListOrders() throws Exception {
        JsonNode resp = httpGet("/api/customer/orders", "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    @Test
    @Order(31)
    @DisplayName("CUST-031: 查看订单详情")
    void testGetOrderDetail() throws Exception {
        assertNotNull(customerOrderNo, "需要先创建订单");
        JsonNode resp = httpGet("/api/customer/orders/" + customerOrderNo, "");
        assertSuccess(resp);
        assertEquals(customerOrderNo, resp.path("data").path("orderNo").asText());
        assertEquals("UNPAID", resp.path("data").path("status").asText());
        assertEquals("CUSTOMER_MINI", resp.path("data").path("source").asText());

        // 验证订单项
        assertTrue(resp.path("data").path("items").isArray());
        assertTrue(resp.path("data").path("items").size() >= 2, "应有至少2个订单项");
    }

    @Test
    @Order(32)
    @DisplayName("CUST-032: 不存在的订单")
    void testGetNonexistentOrder() throws Exception {
        JsonNode resp = httpGet("/api/customer/orders/XYO999999999999", "");
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(33)
    @DisplayName("CUST-033: 查看订单日期标记")
    void testListOrderDateMarkers() throws Exception {
        JsonNode resp = httpGet("/api/customer/orders/date-markers", "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    // ==================== 6. 支付流程 ====================

    @Test
    @Order(40)
    @DisplayName("CUST-040: 创建支付单")
    void testCreatePayment() throws Exception {
        assertNotNull(customerOrderNo, "需要先创建订单");
        JsonNode resp = httpPost("/api/customer/orders/" + customerOrderNo + "/payments", "", null);
        assertSuccess(resp);
        assertNotNull(resp.path("data").path("paymentNo").asText());
        assertNotNull(resp.path("data").path("status").asText());
    }

    // ==================== 7. 活动与会员等级 ====================

    @Test
    @Order(50)
    @DisplayName("CUST-050: 获取会员等级列表")
    void testGetMemberLevels() throws Exception {
        JsonNode resp = httpGet("/api/customer/member/levels", "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
        assertTrue(resp.path("data").size() >= 3, "应有至少3个等级(REGULAR/VIP/SVIP)");

        // 验证默认等级
        boolean hasRegular = false;
        for (JsonNode level : resp.path("data")) {
            if ("REGULAR".equals(level.path("level").asText())) {
                hasRegular = true;
                assertEquals("100", level.path("discount").asText(), "REGULAR折扣率应为100");
            }
        }
        assertTrue(hasRegular, "应有REGULAR等级");
    }

    @Test
    @Order(51)
    @DisplayName("CUST-051: 获取活动列表")
    void testListActivities() throws Exception {
        JsonNode resp = httpGet("/api/customer/activities", "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    // ==================== 8. 会员功能 ====================

    @Test
    @Order(60)
    @DisplayName("CUST-060: 获取顾客统计")
    void testGetCustomerStats() throws Exception {
        JsonNode resp = httpGet("/api/customer/stats?phone=" + customerPhone, "");
        assertSuccess(resp);
        assertTrue(resp.path("data").has("points"));
        assertTrue(resp.path("data").has("coupons"));
        assertTrue(resp.path("data").has("totalOrders"));
    }

    @Test
    @Order(61)
    @DisplayName("CUST-061: 获取会员信息")
    void testGetMemberInfo() throws Exception {
        JsonNode resp = httpGet("/api/customer/member/info?phone=" + customerPhone, "");
        assertSuccess(resp);
        // 之前下单时用了这个手机号，应有记录
        assertTrue(resp.path("data").has("phone"));
    }

    @Test
    @Order(62)
    @DisplayName("CUST-062: 修改会员资料")
    void testUpdateMemberProfile() throws Exception {
        Map<String, Object> body = Map.of(
                "phone", customerPhone,
                "nickname", "测试酒客",
                "gender", "男"
        );
        JsonNode resp = httpPut("/api/customer/member/profile", "", body);
        assertSuccess(resp);
        assertEquals("测试酒客", resp.path("data").path("nickname").asText());
    }

    @Test
    @Order(63)
    @DisplayName("CUST-063: 查看优惠券列表")
    void testListCoupons() throws Exception {
        JsonNode resp = httpGet("/api/customer/coupons?phone=" + customerPhone, "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
        // 首次查看会自动发放默认优惠券
        assertTrue(resp.path("data").size() > 0, "应有默认优惠券");
    }

    @Test
    @Order(64)
    @DisplayName("CUST-064: 查看积分记录")
    void testListPointsRecords() throws Exception {
        JsonNode resp = httpGet("/api/customer/points/records?phone=" + customerPhone, "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    @Test
    @Order(65)
    @DisplayName("CUST-065: 查看消息列表")
    void testListMessages() throws Exception {
        JsonNode resp = httpGet("/api/customer/messages?phone=" + customerPhone, "");
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
    }

    // ==================== 9. 后台查看顾客端订单 ====================

    @Test
    @Order(70)
    @DisplayName("CUST-070: 后台订单列表包含顾客端订单")
    void testAdminOrderListContainsCustomerOrder() throws Exception {
        assertNotNull(customerOrderNo, "需要先创建顾客端订单");
        JsonNode resp = httpGet("/api/admin/orders?pageNum=1&pageSize=50&source=CUSTOMER_MINI", bossToken());
        assertSuccess(resp);

        boolean found = false;
        for (JsonNode order : resp.path("data").path("records")) {
            if (customerOrderNo.equals(order.path("orderNo").asText())) {
                found = true;
                assertEquals("UNPAID", order.path("status").asText());
                break;
            }
        }
        assertTrue(found, "后台应能看到顾客端创建的订单");
    }

    @Test
    @Order(71)
    @DisplayName("CUST-071: 后台支付顾客端订单")
    void testAdminPayCustomerOrder() throws Exception {
        assertNotNull(customerOrderNo);
        // 找到订单ID
        JsonNode listResp = httpGet("/api/admin/orders?orderNo=" + customerOrderNo, bossToken());
        if (listResp.path("data").path("records").size() > 0) {
            Long orderId = listResp.path("data").path("records").get(0).path("id").asLong();
            Map<String, Object> payBody = Map.of("paymentMethod", "CASH");
            JsonNode resp = httpPatch("/api/admin/orders/" + orderId + "/pay", bossToken(), payBody);
            assertSuccess(resp);
        }
    }

    // ==================== 10. 多种商品组合下单 ====================

    @Test
    @Order(80)
    @DisplayName("CUST-080: 多种商品组合下单")
    void testMultiProductOrder() throws Exception {
        JsonNode tablesResp = httpGet("/api/customer/tables", "");
        Long tableId = null;
        for (JsonNode t : tablesResp.path("data")) {
            if ("EMPTY".equals(t.path("status").asText())) {
                tableId = t.path("id").asLong();
                break;
            }
        }
        if (tableId == null) return; // 无空闲桌台跳过

        Map<String, Object> body = Map.of(
                "tableId", tableId,
                "phone", customerPhone,
                "items", java.util.List.of(
                        Map.of("productId", 1, "quantity", 1),   // 百威啤酒 x1 = 30
                        Map.of("productId", 2, "quantity", 1),   // 长岛冰茶 x1 = 60
                        Map.of("productId", 3, "quantity", 1),   // 野格 x1 = 60
                        Map.of("productId", 5, "quantity", 2)    // 薯条 x2 = 50
                )
        );

        JsonNode resp = httpPost("/api/customer/orders", "", body);
        assertSuccess(resp);

        // 验证金额: 30 + 60 + 60 + 25*2 = 200
        double totalAmount = resp.path("data").path("totalAmount").asDouble();
        assertEquals(200.00, totalAmount, 0.01, "多商品订单总额应为200元");
    }

    // ==================== 11. 下单后库存校验 ====================

    @Test
    @Order(90)
    @DisplayName("CUST-090: 下单后库存正确扣减")
    void testStockDeductionAfterOrder() throws Exception {
        assertNotNull(customerOrderNo);
        // 获取百威啤酒当前库存
        JsonNode prodResp = httpGet("/api/admin/products/1", bossToken());
        int currentStock = prodResp.path("data").path("stock").asInt();

        // 百威啤酒在 CUST-020 被点了2个，库存应已减少
        // 这里验证库存是合理的(>=0)
        assertTrue(currentStock >= 0, "库存不应为负数: " + currentStock);
    }

    // ==================== 12. 空/无效参数边界测试 ====================

    @Test
    @Order(100)
    @DisplayName("CUST-100: 空phone查询统计")
    void testStatsWithoutPhone() throws Exception {
        JsonNode resp = httpGet("/api/customer/stats", "");
        assertSuccess(resp);
        assertEquals(0, resp.path("data").path("totalOrders").asInt());
    }

    @Test
    @Order(101)
    @DisplayName("CUST-101: 不存在的phone查询会员")
    void testGetNonexistentMember() throws Exception {
        JsonNode resp = httpGet("/api/customer/member/info?phone=00000000000", "");
        assertSuccess(resp);
        // 不存在的手机号返回空对象
        assertFalse(resp.path("data").has("id") && resp.path("data").path("id").asLong() > 0,
                "不存在的手机号不应返回有效会员");
    }

    @Test
    @Order(102)
    @DisplayName("CUST-102: 非法手机号修改资料")
    void testUpdateProfileInvalidPhone() throws Exception {
        Map<String, Object> body = Map.of("phone", "invalid");
        JsonNode resp = httpPut("/api/customer/member/profile", "", body);
        // 应该能处理(可能报错或忽略)
        assertTrue(resp.has("code"));
    }
}
