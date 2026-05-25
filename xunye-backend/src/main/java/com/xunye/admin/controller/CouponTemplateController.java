package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.CouponTemplateSaveDTO;
import com.xunye.admin.service.CouponTemplateService;
import com.xunye.admin.vo.CouponTemplateVO;
import com.xunye.admin.vo.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupon-templates")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER"})
public class CouponTemplateController {

    private final CouponTemplateService couponTemplateService;

    /**
     * 分页查询优惠券模板
     */
    @GetMapping
    public ApiResponse<PageResult<CouponTemplateVO>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.success(couponTemplateService.getPage(pageNum, pageSize, keyword, type, status));
    }

    /**
     * 获取优惠券模板详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CouponTemplateVO> getDetail(@PathVariable Long id) {
        return ApiResponse.success(couponTemplateService.getDetail(id));
    }

    /**
     * 创建优惠券模板
     */
    @PostMapping
    @AuditLog(operation = "创建优惠券模板", module = "优惠券管理")
    public ApiResponse<Long> create(@Valid @RequestBody CouponTemplateSaveDTO dto) {
        return ApiResponse.success(couponTemplateService.create(dto));
    }

    /**
     * 更新优惠券模板
     */
    @PutMapping("/{id}")
    @AuditLog(operation = "更新优惠券模板", module = "优惠券管理")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CouponTemplateSaveDTO dto) {
        couponTemplateService.update(id, dto);
        return ApiResponse.success();
    }

    /**
     * 删除优惠券模板
     */
    @DeleteMapping("/{id}")
    @AuditLog(operation = "删除优惠券模板", module = "优惠券管理")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        couponTemplateService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 启用/禁用优惠券模板
     */
    @PatchMapping("/{id}/status")
    @AuditLog(operation = "更新优惠券模板状态", module = "优惠券管理")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        couponTemplateService.updateStatus(id, status);
        return ApiResponse.success();
    }

}
