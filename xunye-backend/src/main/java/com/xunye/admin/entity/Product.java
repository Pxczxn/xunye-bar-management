package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 酒水商品实体类
 */
@Data
@TableName("product")
public class Product {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    private String name;

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
     * 状态：ON_SALE上架，OFF_SALE下架
     */
    private String status;

    /**
     * 是否删除：0未删除，1已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
