package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerPhoneLoginDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** Used for verification code login */
    private String verifyCode;

    /** Used for password login */
    private String password;

}
