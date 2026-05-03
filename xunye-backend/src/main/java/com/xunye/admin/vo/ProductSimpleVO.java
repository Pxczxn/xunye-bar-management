package com.xunye.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品简表 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSimpleVO {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品分类
     */
    private String category;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 规格
     */
    private String spec;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 安全库存
     */
    private Integer safeStock;

    /**
     * 状态：ON_SALE-上架，OFF_SALE-下架
     */
    private String status;

}
