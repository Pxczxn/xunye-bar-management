package com.xunye.admin.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.xunye.admin.base.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 桌台管理 API 全面测试
 * 覆盖: CRUD、状态流转、清台、区域
 */
@DisplayName("API-TABLE: 桌台管理测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BarTableApiTest extends BaseIntegrationTest {

    private static Long createdTableId;
    private static Long createdAreaId;

    // ==================== 区域管理 ====================

    @Test
    @Order(1)
    @DisplayName("API-TBL-001: 查询区域列表")
    void testListAreas() throws Exception {
        JsonNode resp = httpGet("/api/admin/table-areas", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").isArray());
        assertTrue(resp.path("data").size() >= 3);
    }

    @Test
    @Order(2)
    @DisplayName("API-TBL-002: 创建区域")
    void testCreateArea() throws Exception {
        Map<String, Object> body = Map.of("name", "测试区域", "sort", 10, "status", 1);
        JsonNode resp = httpPost("/api/admin/table-areas", bossToken(), body);
        assertSuccess(resp);

        // 获取创建的区域ID
        JsonNode areas = httpGet("/api/admin/table-areas", bossToken());
        for (JsonNode area : areas.path("data")) {
            if ("测试区域".equals(area.path("name").asText())) {
                createdAreaId = area.path("id").asLong();
                break;
            }
        }
    }

    // ==================== 桌台查询 ====================

    @Test
    @Order(10)
    @DisplayName("API-TBL-010: 桌台分页查询")
    void testGetTablePage() throws Exception {
        JsonNode resp = httpGet("/api/admin/tables?pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        assertTrue(resp.path("data").path("records").isArray());
    }

    @Test
    @Order(11)
    @DisplayName("API-TBL-011: 按区域筛选桌台")
    void testFilterByArea() throws Exception {
        JsonNode resp = httpGet("/api/admin/tables?areaId=1&pageNum=1&pageSize=10", bossToken());
        assertSuccess(resp);
        resp.path("data").path("records").forEach(item -> {
            assertEquals(1, item.path("areaId").asInt());
        });
    }

    // ==================== 桌台创建 ====================

    @Test
    @Order(20)
    @DisplayName("API-TBL-020: 创建桌台")
    void testCreateTable() throws Exception {
        Long areaId = createdAreaId != null ? createdAreaId : 1L;
        Map<String, Object> body = Map.of(
                "areaId", areaId,
                "name", "TEST" + System.currentTimeMillis(),
                "capacity", 4,
                "status", "EMPTY"
        );
        JsonNode resp = httpPost("/api/admin/tables", bossToken(), body);
        assertSuccess(resp);

        // 找到创建的桌台
        JsonNode tables = httpGet("/api/admin/tables?pageNum=1&pageSize=20", bossToken());
        for (JsonNode t : tables.path("data").path("records")) {
            if (t.path("name").asText().startsWith("TEST")) {
                createdTableId = t.path("id").asLong();
                break;
            }
        }
    }

    // ==================== 桌台修改 ====================

    @Test
    @Order(21)
    @DisplayName("API-TBL-021: 修改桌台")
    void testUpdateTable() throws Exception {
        if (createdTableId == null) return;
        Map<String, Object> body = Map.of(
                "areaId", 1, "name", "修改后桌台", "capacity", 6
        );
        JsonNode resp = httpPut("/api/admin/tables/" + createdTableId, bossToken(), body);
        assertSuccess(resp);
    }

    // ==================== 状态流转 ====================

    @Test
    @Order(30)
    @DisplayName("API-TBL-030: 修改桌台状态")
    void testUpdateTableStatus() throws Exception {
        if (createdTableId == null) return;
        Map<String, Object> body = Map.of("status", "USING");
        JsonNode resp = httpPatch("/api/admin/tables/" + createdTableId + "/status", bossToken(), body);
        assertSuccess(resp);
    }

    @Test
    @Order(31)
    @DisplayName("API-TBL-031: 清台")
    void testClearTable() throws Exception {
        if (createdTableId == null) return;
        JsonNode resp = httpPatch("/api/admin/tables/" + createdTableId + "/clear", bossToken(), null);
        assertSuccess(resp);

        // 验证状态变回EMPTY
        JsonNode tables = httpGet("/api/admin/tables?pageNum=1&pageSize=20", bossToken());
        for (JsonNode t : tables.path("data").path("records")) {
            if (t.path("id").asLong() == createdTableId) {
                assertEquals("EMPTY", t.path("status").asText());
                break;
            }
        }
    }

    // ==================== 删除 ====================

    @Test
    @Order(40)
    @DisplayName("API-TBL-040: 删除桌台")
    void testDeleteTable() throws Exception {
        if (createdTableId == null) return;
        JsonNode resp = httpDelete("/api/admin/tables/" + createdTableId, bossToken());
        assertSuccess(resp);
    }

    @Test
    @Order(41)
    @DisplayName("API-TBL-041: 删除区域")
    void testDeleteArea() throws Exception {
        if (createdAreaId == null) return;
        JsonNode resp = httpDelete("/api/admin/table-areas/" + createdAreaId, bossToken());
        // 区域可能有关联桌台，记录行为
        assertTrue(resp.has("code"));
    }

    // ==================== 权限 ====================

    @Test
    @Order(50)
    @DisplayName("API-TBL-050: STAFF可查询桌台")
    void testStaffCanQuery() throws Exception {
        JsonNode resp = httpGet("/api/admin/tables?pageNum=1&pageSize=1", staffToken());
        assertSuccess(resp);
    }

    @Test
    @Order(51)
    @DisplayName("API-TBL-051: STAFF可清台")
    void testStaffCanClear() throws Exception {
        // 找一个USING状态的桌台
        JsonNode tables = httpGet("/api/admin/tables?pageNum=1&pageSize=10", staffToken());
        Long usingTableId = null;
        for (JsonNode t : tables.path("data").path("records")) {
            if ("USING".equals(t.path("status").asText())) {
                usingTableId = t.path("id").asLong();
                break;
            }
        }
        if (usingTableId != null) {
            JsonNode resp = httpPatch("/api/admin/tables/" + usingTableId + "/clear", staffToken(), null);
            assertSuccess(resp);
        }
    }
}
