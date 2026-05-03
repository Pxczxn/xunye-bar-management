package com.xunye.admin.dto;

import lombok.Data;

/**
 * 商品查询 DTO
 */
@Data
public class ProductQueryDTO {

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页数量，默认10
     */
    private Integer pageSize = 10;

    /**
     * 关键词（商品名称模糊搜索）
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态：ON_SALE / OFF_SALE
     */
    private String status;

}
