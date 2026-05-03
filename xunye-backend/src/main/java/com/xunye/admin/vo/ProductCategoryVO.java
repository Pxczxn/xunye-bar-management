package com.xunye.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类 VO（列表查询）
 */
@Data
public class ProductCategoryVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 分类名称
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
