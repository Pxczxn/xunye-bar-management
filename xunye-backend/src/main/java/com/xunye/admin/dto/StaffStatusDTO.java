package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 员工状态修改 DTO
 */
@Data
public class StaffStatusDTO {

    /**
     * 状态：1启用，0禁用
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态只能是 0 或 1")
    private String status;

}
