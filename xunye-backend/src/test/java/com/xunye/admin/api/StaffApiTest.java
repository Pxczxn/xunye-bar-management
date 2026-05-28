package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 员工管理 API 全面测试
 * 覆盖: CRUD、状态、密码重置、权限
 */
@DisplayName("API-STAFF: 员工管理测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffApiTest extends BaseIntegrationTest {

    private static Long createdStaffId;
    private static final String TEST_USERNAME = "teststaff_" + System.currentTimeMillis();

    // ==================== 查询 ====================

    @Test
    @Order(1)
    @DisplayName("API-STAFF-001: 分页查询员工")
    void testGetStaffPage() throws Exception {
        JsonNode resp = httpGet("/api/admin/staff?pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").path("records").isArray());
        assertTrue(resp.path("data").path("total").asLong() >= 4);
    }

    @Test
    @Order(2)
    @DisplayName("API-STAFF-002: 按角色筛选")
    void testFilterByRole() throws Exception {
        JsonNode resp = httpGet("/api/admin/staff?role=STAFF", bossToken());
        assertSuccess(resp);
        resp.path("data").path("records").forEach(item -> {
            assertEquals("STAFF", item.path("role").asText());
        });
    }

    @Test
    @Order(3)
    @DisplayName("API-STAFF-003: 员工详情")
    void testGetStaffDetail() throws Exception {
        JsonNode list = httpGet("/api/admin/staff?pageNum=1&pageSize=1", bossToken());
        Long id = list.path("data").path("records").get(0).path("id").asLong();
        JsonNode resp = httpGet("/api/admin/staff/" + id, bossToken());
        assertSuccess(resp);
        assertNotNull(resp.path("data").path("username").asText());
    }

    // ==================== 创建 ====================

    @Test
    @Order(10)
    @DisplayName("API-STAFF-010: 创建员工(正常)")
    void testCreateStaff() throws Exception {
        Map<String, Object> body = Map.of(
                "username", TEST_USERNAME,
                "password", "test123",
                "nickname", "测试员工",
                "role", "STAFF",
                "status", "1"
        );
        JsonNode resp = httpPost("/api/admin/staff", bossToken(), body);
        assertSuccess(resp);

        // 验证创建成功
        JsonNode list = httpGet("/api/admin/staff?keyword=" + TEST_USERNAME, bossToken());
        assertTrue(list.path("data").path("records").size() > 0);
        createdStaffId = list.path("data").path("records").get(0).path("id").asLong();
    }

    @Test
    @Order(11)
    @DisplayName("API-STAFF-011: 创建重复用户名")
    void testCreateDuplicateUsername() throws Exception {
        Map<String, Object> body = Map.of(
                "username", TEST_USERNAME,
                "password", "test123",
                "nickname", "重复员工",
                "role", "STAFF",
                "status", "1"
        );
        JsonNode resp = httpPost("/api/admin/staff", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(12)
    @DisplayName("API-STAFF-012: 创建-用户名为空")
    void testCreateEmptyUsername() throws Exception {
        Map<String, Object> body = Map.of(
                "username", "",
                "password", "test123",
                "nickname", "空用户名",
                "role", "STAFF",
                "status", "1"
        );
        JsonNode resp = httpPost("/api/admin/staff", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    @Test
    @Order(13)
    @DisplayName("API-STAFF-013: 创建-无效角色")
    void testCreateInvalidRole() throws Exception {
        Map<String, Object> body = Map.of(
                "username", "invalidrole_" + System.currentTimeMillis(),
                "password", "test123",
                "nickname", "无效角色",
                "role", "INVALID",
                "status", "1"
        );
        JsonNode resp = httpPost("/api/admin/staff", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 修改 ====================

    @Test
    @Order(20)
    @DisplayName("API-STAFF-020: 修改员工")
    void testUpdateStaff() throws Exception {
        if (createdStaffId == null) return;
        Map<String, Object> body = Map.of(
                "nickname", "修改后昵称",
                "role", "STAFF"
        );
        JsonNode resp = httpPut("/api/admin/staff/" + createdStaffId, bossToken(), body);
        assertSuccess(resp);
    }

    @Test
    @Order(21)
    @DisplayName("API-STAFF-021: 修改不存在的员工")
    void testUpdateNonexistentStaff() throws Exception {
        Map<String, Object> body = Map.of("nickname", "不存在", "role", "STAFF");
        JsonNode resp = httpPut("/api/admin/staff/99999", bossToken(), body);
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 状态 ====================

    @Test
    @Order(30)
    @DisplayName("API-STAFF-030: 禁用员工")
    void testDisableStaff() throws Exception {
        if (createdStaffId == null) return;
        Map<String, Object> body = Map.of("status", "0");
        JsonNode resp = httpPatch("/api/admin/staff/" + createdStaffId + "/status", bossToken(), body);
        assertSuccess(resp);
    }

    @Test
    @Order(31)
    @DisplayName("API-STAFF-031: 启用员工")
    void testEnableStaff() throws Exception {
        if (createdStaffId == null) return;
        Map<String, Object> body = Map.of("status", "1");
        JsonNode resp = httpPatch("/api/admin/staff/" + createdStaffId + "/status", bossToken(), body);
        assertSuccess(resp);
    }

    // ==================== 密码重置 ====================

    @Test
    @Order(40)
    @DisplayName("API-STAFF-040: 重置密码")
    void testResetPassword() throws Exception {
        if (createdStaffId == null) return;
        Map<String, Object> body = Map.of("password", "newpass123");
        JsonNode resp = httpPatch("/api/admin/staff/" + createdStaffId + "/reset-password", bossToken(), body);
        assertSuccess(resp);
    }

    // ==================== 删除 ====================

    @Test
    @Order(50)
    @DisplayName("API-STAFF-050: 删除员工")
    void testDeleteStaff() throws Exception {
        if (createdStaffId == null) return;
        JsonNode resp = httpDelete("/api/admin/staff/" + createdStaffId, bossToken());
        assertSuccess(resp);
    }

    @Test
    @Order(51)
    @DisplayName("API-STAFF-051: 删除不存在的员工")
    void testDeleteNonexistentStaff() throws Exception {
        JsonNode resp = httpDelete("/api/admin/staff/99999", bossToken());
        assertNotEquals(200, resp.path("code").asInt());
    }

    // ==================== 权限 ====================

    @Test
    @Order(60)
    @DisplayName("API-STAFF-060: MANAGER不能访问员工管理")
    void testManagerCannotAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/staff", managerToken());
        assertEquals(403, resp.path("code").asInt());
    }

    @Test
    @Order(61)
    @DisplayName("API-STAFF-061: STAFF不能访问员工管理")
    void testStaffCannotAccess() throws Exception {
        JsonNode resp = httpGet("/api/admin/staff", staffToken());
        assertEquals(403, resp.path("code").asInt());
    }
}
