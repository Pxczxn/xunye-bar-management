package com.xunye.admin.dto;

import lombok.Data;

@Data
public class CustomerWxLoginDTO {

    private String code;

    private String customerNo;

    private String phone;

    private String verifyCode;

    private String nickname;

    private String avatar;

}
