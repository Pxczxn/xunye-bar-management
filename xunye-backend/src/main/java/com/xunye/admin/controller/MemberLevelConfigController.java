package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.MemberLevelConfigSaveDTO;
import com.xunye.admin.service.MemberLevelConfigService;
import com.xunye.admin.vo.MemberLevelConfigVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/member-level-configs")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER"})
public class MemberLevelConfigController {

    private final MemberLevelConfigService memberLevelConfigService;

    /**
     * 获取所有会员等级配置
     */
    @GetMapping
    public ApiResponse<List<MemberLevelConfigVO>> listAll() {
        return ApiResponse.success(memberLevelConfigService.listAll());
    }

    /**
     * 获取会员等级配置详情
     */
    @GetMapping("/{id}")
    public ApiResponse<MemberLevelConfigVO> getDetail(@PathVariable Long id) {
        return ApiResponse.success(memberLevelConfigService.getDetail(id));
    }

    /**
     * 创建会员等级配置
     */
    @PostMapping
    @AuditLog(operation = "创建会员等级配置", module = "会员管理")
    public ApiResponse<Long> create(@Valid @RequestBody MemberLevelConfigSaveDTO dto) {
        return ApiResponse.success(memberLevelConfigService.create(dto));
    }

    /**
     * 更新会员等级配置
     */
    @PutMapping("/{id}")
    @AuditLog(operation = "更新会员等级配置", module = "会员管理")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody MemberLevelConfigSaveDTO dto) {
        memberLevelConfigService.update(id, dto);
        return ApiResponse.success();
    }

    /**
     * 删除会员等级配置
     */
    @DeleteMapping("/{id}")
    @AuditLog(operation = "删除会员等级配置", module = "会员管理")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        memberLevelConfigService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 启用/禁用会员等级配置
     */
    @PatchMapping("/{id}/status")
    @AuditLog(operation = "更新会员等级配置状态", module = "会员管理")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        memberLevelConfigService.updateStatus(id, status);
        return ApiResponse.success();
    }

}
