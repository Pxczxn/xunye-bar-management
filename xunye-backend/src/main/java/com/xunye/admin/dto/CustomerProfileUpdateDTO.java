package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerProfileUpdateDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Size(max = 50, message = "昵称最多50个字符")
    private String nickname;

    private String avatar;

    private LocalDate birthday;

    @Size(max = 20, message = "性别最多20个字符")
    private String gender;

    @Size(max = 100, message = "偏好最多100个字符")
    private String favoriteTaste;

    @Size(max = 50, message = "常用桌台最多50个字符")
    private String favoriteTable;
}
