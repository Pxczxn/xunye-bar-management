package com.xunye.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工分页查询 VO
 */
@Data
public class StaffPageVO {

    private Long id;

    private String username;

    private String nickname;

    private String role;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

}
