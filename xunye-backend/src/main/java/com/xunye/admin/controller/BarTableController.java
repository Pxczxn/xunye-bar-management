package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.BarTableQueryDTO;
import com.xunye.admin.dto.BarTableSaveDTO;
import com.xunye.admin.dto.BarTableStatusDTO;
import com.xunye.admin.service.BarTableService;
import com.xunye.admin.vo.BarTableVO;
import com.xunye.admin.vo.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 桌台控制器
 */
@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
public class BarTableController {

    private final BarTableService barTableService;

    /**
     * 桌台分页查询
     */
    @GetMapping
    @RequireRole({"BOSS", "MANAGER", "STAFF"})
    public ApiResponse<PageResult<BarTableVO>> getTablePage(BarTableQueryDTO queryDTO) {
        return ApiResponse.success(barTableService.getTablePage(queryDTO));
    }

    /**
     * 新增桌台
     */
    @PostMapping
    @RequireRole({"BOSS", "MANAGER"})
    @AuditLog(operation = "新增桌台", module = "桌台管理")
    public ApiResponse<Void> createTable(@Valid @RequestBody BarTableSaveDTO dto) {
        barTableService.createTable(dto);
        return ApiResponse.success();
    }

    /**
     * 修改桌台
     */
    @PutMapping("/{id}")
    @RequireRole({"BOSS", "MANAGER"})
    @AuditLog(operation = "修改桌台", module = "桌台管理")
    public ApiResponse<Void> updateTable(@PathVariable Long id, @Valid @RequestBody BarTableSaveDTO dto) {
        barTableService.updateTable(id, dto);
        return ApiResponse.success();
    }

    /**
     * 修改桌台状态
     */
    @PatchMapping("/{id}/status")
    @RequireRole({"BOSS", "MANAGER", "STAFF"})
    @AuditLog(operation = "修改桌台状态", module = "桌台管理")
    public ApiResponse<Void> updateTableStatus(@PathVariable Long id, @Valid @RequestBody BarTableStatusDTO dto) {
        barTableService.updateTableStatus(id, dto);
        return ApiResponse.success();
    }

    /**
     * 清台
     */
    @PatchMapping("/{id}/clear")
    @RequireRole({"BOSS", "MANAGER", "STAFF"})
    @AuditLog(operation = "清台", module = "桌台管理")
    public ApiResponse<Void> clearTable(@PathVariable Long id) {
        barTableService.clearTable(id);
        return ApiResponse.success();
    }

    /**
     * 删除桌台
     */
    @DeleteMapping("/{id}")
    @RequireRole({"BOSS", "MANAGER"})
    @AuditLog(operation = "删除桌台", module = "桌台管理")
    public ApiResponse<Void> deleteTable(@PathVariable Long id) {
        barTableService.deleteTable(id);
        return ApiResponse.success();
    }

}
