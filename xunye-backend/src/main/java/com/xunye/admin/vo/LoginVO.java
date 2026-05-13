package com.xunye.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /**
     * 认证token
     */
    private String token;

    /**
     * 用户信息
     */
    private LoginUserVO user;

    /**
     * 登录用户信息子对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginUserVO {
        private Long id;
        private String username;
        private String nickname;
        private String role;
    }

}
