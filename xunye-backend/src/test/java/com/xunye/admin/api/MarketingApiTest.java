package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 会员营销模块 API 全面测试
 * 覆盖: 优惠券模板、折扣规则、会员等级配置
 */
@DisplayName("API-MARKETING: 会员营销模块测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MarketingApiTest extends BaseIntegrationTest {

    private static Long couponTemplateId;
    private static Long discountRuleId;
    private static Long memberLevelId;

    // ==================== 优惠券模板 ====================

    @Test
    @Order(1)
    @DisplayName("API-COUP-001: 查询优惠券模板列表")
    void testListCouponTemplates() throws Exception {
        JsonNode resp = httpGet("/api/admin/coupon-templates?pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").path("records").isArray());
    }

    @Test
    @Order(2)
    @DisplayName("API-COUP-002: 创建优惠券模板")
    void testCreateCouponTemplate() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "满100减20",
                "title", "满100减20优惠券",
                "type", "FIXED",
                "discountAmount", new BigDecimal("20.00"),
                "minAmount", new BigDecimal("100.00"),
                "scopeType", "ALL",
                "issueType", "MANUAL",
                "validDays", 30,
                "status", 1
        );
        JsonNode resp = httpPost("/api/admin/coupon-templates", bossToken(), body);
        assertSuccess(resp);
        couponTemplateId = resp.path("data").asLong();
    }

    @Test
    @Order(3)
    @DisplayName("API-COUP-003: 获取优惠券模板详情")
    void testGetCouponTemplateDetail() throws Exception {
        if (couponTemplateId == null) return;
        JsonNode resp = httpGet("/api/admin/coupon-templates/" + couponTemplateId, bossToken());
        assertSuccess(resp);
        assertEquals("满100减20", resp.path("data").path("name").asText());
    }

    @Test
    @Order(4)
    @DisplayName("API-COUP-004: 修改优惠券模板")
    void testUpdateCouponTemplate() throws Exception {
        if (couponTemplateId == null) return;
        Map<String, Object> body = Map.of(
                "name", "满200减50",
                "title", "满200减50优惠券",
                "type", "FIXED",
                "discountAmount", new BigDecimal("50.00"),
                "minAmount", new BigDecimal("200.00"),
                "scopeType", "ALL",
                "issueType", "MANUAL",
                "validDays", 60,
                "status", 1
        );
        JsonNode resp = httpPut("/api/admin/coupon-templates/" + couponTemplateId, bossToken(), body);
        assertSuccess(resp);
    }

    @Test
    @Order(5)
    @DisplayName("API-COUP-005: 启用/禁用优惠券模板")
    void testToggleCouponTemplateStatus() throws Exception {
        if (couponTemplateId == null) return;
        JsonNode resp = httpPatch("/api/admin/coupon-templates/" + couponTemplateId + "/status?status=0", bossToken(), null);
        assertSuccess(resp);
    }

    @Test
    @Order(6)
    @DisplayName("API-COUP-006: 删除优惠券模板")
    void testDeleteCouponTemplate() throws Exception {
        if (couponTemplateId == null) return;
        JsonNode resp = httpDelete("/api/admin/coupon-templates/" + couponTemplateId, bossToken());
        assertSuccess(resp);
    }

    // ==================== 折扣规则 ====================

    @Test
    @Order(10)
    @DisplayName("API-DR-001: 查询折扣规则列表")
    void testListDiscountRules() throws Exception {
        JsonNode resp = httpGet("/api/admin/discount-rules?pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").path("records").isArray());
    }

    @Test
    @Order(11)
    @DisplayName("API-DR-002: 创建折扣规则")
    void testCreateDiscountRule() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "满减规则",
                "ruleType", "FULL_REDUCTION",
                "priority", 1,
                "conditions", Map.of("minAmount", 100, "reduceAmount", 10),
                "status", 1
        );
        JsonNode resp = httpPost("/api/admin/discount-rules", bossToken(), body);
        assertSuccess(resp);
        discountRuleId = resp.path("data").asLong();
    }

    @Test
    @Order(12)
    @DisplayName("API-DR-003: 获取折扣规则详情")
    void testGetDiscountRuleDetail() throws Exception {
        if (discountRuleId == null) return;
        JsonNode resp = httpGet("/api/admin/discount-rules/" + discountRuleId, bossToken());
        assertSuccess(resp);
        assertEquals("满减规则", resp.path("data").path("name").asText());
    }

    @Test
    @Order(13)
    @DisplayName("API-DR-004: 修改折扣规则")
    void testUpdateDiscountRule() throws Exception {
        if (discountRuleId == null) return;
        Map<String, Object> body = Map.of(
                "name", "修改后满减规则",
                "ruleType", "FULL_REDUCTION",
                "priority", 2,
                "status", 1
        );
        JsonNode resp = httpPut("/api/admin/discount-rules/" + discountRuleId, bossToken(), body);
        assertSuccess(resp);
    }

    @Test
    @Order(14)
    @DisplayName("API-DR-005: 启用/禁用折扣规则")
    void testToggleDiscountRuleStatus() throws Exception {
        if (discountRuleId == null) return;
        JsonNode resp = httpPatch("/api/admin/discount-rules/" + discountRuleId + "/status?status=0", bossToken(), null);
        assertSuccess(resp);
    }

    @Test
    @Order(15)
    @DisplayName("API-DR-006: 删除折扣规则")
    void testDeleteDiscountRule() throws Exception {
        if (discountRuleId == null) return;
        JsonNode resp = httpDelete("/api/admin/discount-rules/" + discountRuleId, bossToken());
        assertSuccess(resp);
    }

    // ==================== 会员等级配置 ====================

    @Test
    @Order(20)
    @DisplayName("API-MLV-001: 获取会员等级列表")
    void testListMemberLevels() throws Exception {
        JsonNode resp = httpGet("/api/admin/member-level-configs", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
        assertTrue(resp.path("data").size() >= 3);
    }

    @Test
    @Order(21)
    @DisplayName("API-MLV-002: 获取等级详情")
    void testGetMemberLevelDetail() throws Exception {
        JsonNode list = httpGet("/api/admin/member-level-configs", bossToken());
        Long id = list.path("data").get(0).path("id").asLong();
        JsonNode resp = httpGet("/api/admin/member-level-configs/" + id, bossToken());
        assertSuccess(resp);
        memberLevelId = id;
    }

    @Test
    @Order(22)
    @DisplayName("API-MLV-003: 创建会员等级配置")
    void testCreateMemberLevel() throws Exception {
        Map<String, Object> body = Map.of(
                "level", "PLATINUM",
                "name", "铂金会员",
                "minAmount", new BigDecimal("10000"),
                "discount", new BigDecimal("85.00"),
                "pointsRate", new BigDecimal("250.00"),
                "description", "累计消费满10000元",
                "sort", 4,
                "status", 1
        );
        JsonNode resp = httpPost("/api/admin/member-level-configs", bossToken(), body);
        assertSuccess(resp);
    }

    @Test
    @Order(23)
    @DisplayName("API-MLV-004: 修改会员等级折扣率")
    void testUpdateMemberLevelDiscount() throws Exception {
        if (memberLevelId == null) return;
        Map<String, Object> body = Map.of(
                "level", "REGULAR",
                "name", "普通会员",
                "minAmount", new BigDecimal("0"),
                "discount", new BigDecimal("98.00"),
                "pointsRate", new BigDecimal("100.00"),
                "description", "新注册默认",
                "sort", 1,
                "status", 1
        );
        JsonNode resp = httpPut("/api/admin/member-level-configs/" + memberLevelId, bossToken(), body);
        assertSuccess(resp);

        // 恢复原值
        body = Map.of(
                "level", "REGULAR",
                "name", "普通会员",
                "minAmount", new BigDecimal("0"),
                "discount", new BigDecimal("100.00"),
                "pointsRate", new BigDecimal("100.00"),
                "description", "新注册默认会员等级",
                "sort", 1,
                "status", 1
        );
        httpPut("/api/admin/member-level-configs/" + memberLevelId, bossToken(), body);
    }

    // ==================== 权限 ====================

    @Test
    @Order(30)
    @DisplayName("API-MKT-030: STAFF不可访问营销模块")
    void testStaffCannotAccessMarketing() throws Exception {
        JsonNode r1 = httpGet("/api/admin/coupon-templates", staffToken());
        assertEquals(403, r1.path("code").asInt());
        JsonNode r2 = httpGet("/api/admin/discount-rules", staffToken());
        assertEquals(403, r2.path("code").asInt());
        JsonNode r3 = httpGet("/api/admin/member-level-configs", staffToken());
        assertEquals(403, r3.path("code").asInt());
    }

    @Test
    @Order(31)
    @DisplayName("API-MKT-031: MANAGER可访问营销模块")
    void testManagerCanAccessMarketing() throws Exception {
        JsonNode r1 = httpGet("/api/admin/coupon-templates", managerToken());
        assertSuccess(r1);
        JsonNode r2 = httpGet("/api/admin/discount-rules", managerToken());
        assertSuccess(r2);
    }
}
