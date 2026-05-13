package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 员工重置密码 DTO
 */
@Data
public class StaffPasswordDTO {

    /**
     * 新密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

}
