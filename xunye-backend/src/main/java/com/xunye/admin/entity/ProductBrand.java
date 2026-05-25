package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品品牌历史实体类
 */
@Data
@TableName("product_brand")
public class ProductBrand extends BaseEntity {

    /**
     * 品牌名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

}
