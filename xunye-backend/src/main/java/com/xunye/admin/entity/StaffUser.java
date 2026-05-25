package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工用户实体类
 */
@Data
@TableName("staff_user")
public class StaffUser extends BaseEntity {

    /**
     * 登录账号
     */
    private String username;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 显示名称
     */
    private String nickname;

    /**
     * 角色：BOSS、MANAGER、STAFF
     */
    private String role;

    /**
     * 账号状态：1启用，0禁用
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

}
