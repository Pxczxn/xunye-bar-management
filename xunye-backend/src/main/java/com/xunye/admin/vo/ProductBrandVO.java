package com.xunye.admin.vo;

import lombok.Data;

/**
 * 品牌 VO
 */
@Data
public class ProductBrandVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 品牌名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

}
