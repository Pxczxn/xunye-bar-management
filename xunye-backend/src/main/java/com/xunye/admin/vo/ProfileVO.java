package com.xunye.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileVO {

    private Long id;

    private String username;

    private String nickname;

    private String role;

}
