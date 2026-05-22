package com.xunye.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品分页 VO
 */
@Data
public class ProductPageVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称（联表查询）
     */
    private String categoryName;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 规格
     */
    private String spec;

    /**
     * 售价
     */
    private BigDecimal price;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 当前库存
     */
    private Integer stock;

    /**
     * 安全库存
     */
    private Integer safeStock;

    /**
     * 单位
     */
    private String unit;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：ON_SALE / OFF_SALE
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

}
