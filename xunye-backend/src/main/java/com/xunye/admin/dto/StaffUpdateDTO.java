package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 员工编辑 DTO
 */
@Data
public class StaffUpdateDTO {

    /**
     * 显示名称
     */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /**
     * 角色
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(BOSS|MANAGER|STAFF)$", message = "角色只能是 BOSS、MANAGER 或 STAFF")
    private String role;

    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态只能是 0 或 1")
    private String status;

}
