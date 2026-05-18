package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.MemberLevelUpdateDTO;
import com.xunye.admin.service.CustomerMemberService;
import com.xunye.admin.vo.CustomerMemberVO;
import com.xunye.admin.vo.MemberLevelVO;
import com.xunye.admin.vo.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER"})
public class CustomerMemberController {

    private final CustomerMemberService customerMemberService;

    @GetMapping
    public ApiResponse<PageResult<CustomerMemberVO>> getCustomerPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String memberLevel) {
        return ApiResponse.success(customerMemberService.getCustomerPage(pageNum, pageSize, keyword, memberLevel));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerMemberVO> getCustomerDetail(@PathVariable Long id) {
        return ApiResponse.success(customerMemberService.getCustomerDetail(id));
    }

    @GetMapping("/levels")
    public ApiResponse<List<MemberLevelVO>> listMemberLevels() {
        return ApiResponse.success(customerMemberService.listMemberLevels());
    }

    @PatchMapping("/{id}/level")
    @AuditLog(operation = "修改会员等级", module = "会员管理")
    public ApiResponse<Void> updateMemberLevel(@PathVariable Long id, @Valid @RequestBody MemberLevelUpdateDTO dto) {
        customerMemberService.updateMemberLevel(id, dto.getMemberLevel());
        return ApiResponse.success();
    }

}
