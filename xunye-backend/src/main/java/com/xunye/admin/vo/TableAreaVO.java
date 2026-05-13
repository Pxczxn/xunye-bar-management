package com.xunye.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 桌台区域 VO
 */
@Data
public class TableAreaVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：1启用，0禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
