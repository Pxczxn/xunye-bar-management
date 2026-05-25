package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.DiscountRuleSaveDTO;
import com.xunye.admin.service.DiscountRuleService;
import com.xunye.admin.vo.DiscountRuleVO;
import com.xunye.admin.vo.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/discount-rules")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER"})
public class DiscountRuleController {

    private final DiscountRuleService discountRuleService;

    /**
     * 分页查询折扣规则
     */
    @GetMapping
    public ApiResponse<PageResult<DiscountRuleVO>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(discountRuleService.getPage(pageNum, pageSize, keyword, ruleType, status));
    }

    /**
     * 获取所有折扣规则
     */
    @GetMapping("/all")
    public ApiResponse<List<DiscountRuleVO>> listAll() {
        return ApiResponse.success(discountRuleService.listAll());
    }

    /**
     * 获取折扣规则详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DiscountRuleVO> getDetail(@PathVariable Long id) {
        return ApiResponse.success(discountRuleService.getDetail(id));
    }

    /**
     * 创建折扣规则
     */
    @PostMapping
    @AuditLog(operation = "创建折扣规则", module = "折扣管理")
    public ApiResponse<Long> create(@Valid @RequestBody DiscountRuleSaveDTO dto) {
        return ApiResponse.success(discountRuleService.create(dto));
    }

    /**
     * 更新折扣规则
     */
    @PutMapping("/{id}")
    @AuditLog(operation = "更新折扣规则", module = "折扣管理")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody DiscountRuleSaveDTO dto) {
        discountRuleService.update(id, dto);
        return ApiResponse.success();
    }

    /**
     * 删除折扣规则
     */
    @DeleteMapping("/{id}")
    @AuditLog(operation = "删除折扣规则", module = "折扣管理")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        discountRuleService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 启用/禁用折扣规则
     */
    @PatchMapping("/{id}/status")
    @AuditLog(operation = "更新折扣规则状态", module = "折扣管理")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        discountRuleService.updateStatus(id, status);
        return ApiResponse.success();
    }

}
