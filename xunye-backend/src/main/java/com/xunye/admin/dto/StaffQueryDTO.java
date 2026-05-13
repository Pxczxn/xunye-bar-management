package com.xunye.admin.dto;

import lombok.Data;

/**
 * 员工查询 DTO
 */
@Data
public class StaffQueryDTO {

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页数量，默认10
     */
    private Integer pageSize = 10;

    /**
     * 搜索关键词（用户名/昵称模糊匹配）
     */
    private String keyword;

    /**
     * 角色筛选
     */
    private String role;

    /**
     * 状态筛选
     */
    private String status;

}
