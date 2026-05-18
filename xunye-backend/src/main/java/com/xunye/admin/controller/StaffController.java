package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.*;
import com.xunye.admin.service.StaffService;
import com.xunye.admin.vo.PageResult;
import com.xunye.admin.vo.StaffPageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 员工账号控制器
 */
@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
@RequireRole({"BOSS"})
public class StaffController {

    private final StaffService staffService;

    /**
     * 分页查询员工
     */
    @GetMapping
    public ApiResponse<PageResult<StaffPageVO>> getStaffPage(StaffQueryDTO queryDTO) {
        return ApiResponse.success(staffService.getStaffPage(queryDTO));
    }

    /**
     * 查询员工详情
     */
    @GetMapping("/{id}")
    public ApiResponse<StaffPageVO> getStaffDetail(@PathVariable Long id) {
        return ApiResponse.success(staffService.getStaffDetail(id));
    }

    /**
     * 新增员工
     */
    @PostMapping
    @AuditLog(operation = "新增员工", module = "员工管理")
    public ApiResponse<Void> createStaff(@Valid @RequestBody StaffSaveDTO dto) {
        staffService.createStaff(dto);
        return ApiResponse.success();
    }

    /**
     * 编辑员工
     */
    @PutMapping("/{id}")
    @AuditLog(operation = "编辑员工", module = "员工管理")
    public ApiResponse<Void> updateStaff(@PathVariable Long id, @Valid @RequestBody StaffUpdateDTO dto) {
        staffService.updateStaff(id, dto);
        return ApiResponse.success();
    }

    /**
     * 更新员工状态
     */
    @PatchMapping("/{id}/status")
    @AuditLog(operation = "更新员工状态", module = "员工管理")
    public ApiResponse<Void> updateStaffStatus(@PathVariable Long id, @Valid @RequestBody StaffStatusDTO dto) {
        staffService.updateStaffStatus(id, dto);
        return ApiResponse.success();
    }

    /**
     * 重置员工密码
     */
    @PatchMapping("/{id}/reset-password")
    @AuditLog(operation = "重置员工密码", module = "员工管理")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody StaffPasswordDTO dto) {
        staffService.resetPassword(id, dto);
        return ApiResponse.success();
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    @AuditLog(operation = "删除员工", module = "员工管理")
    public ApiResponse<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ApiResponse.success();
    }

}
