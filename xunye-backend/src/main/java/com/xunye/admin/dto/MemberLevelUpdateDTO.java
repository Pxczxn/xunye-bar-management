package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberLevelUpdateDTO {

    @NotBlank(message = "会员等级不能为空")
    private String memberLevel;

}
