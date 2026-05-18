package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.ActivitySaveDTO;
import com.xunye.admin.service.MemberActivityService;
import com.xunye.admin.vo.ActivityVO;
import com.xunye.admin.vo.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/activities")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER"})
public class MemberActivityController {

    private final MemberActivityService memberActivityService;

    @GetMapping
    public ApiResponse<PageResult<ActivityVO>> getActivityPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(memberActivityService.getActivityPage(pageNum, pageSize, keyword, type, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<ActivityVO> getActivityDetail(@PathVariable Long id) {
        return ApiResponse.success(memberActivityService.getActivityDetail(id));
    }

    @PostMapping
    @AuditLog(operation = "新增活动", module = "活动管理")
    public ApiResponse<Void> createActivity(@Valid @RequestBody ActivitySaveDTO dto) {
        memberActivityService.createActivity(dto);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    @AuditLog(operation = "编辑活动", module = "活动管理")
    public ApiResponse<Void> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivitySaveDTO dto) {
        memberActivityService.updateActivity(id, dto);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @AuditLog(operation = "删除活动", module = "活动管理")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        memberActivityService.deleteActivity(id);
        return ApiResponse.success();
    }

}
