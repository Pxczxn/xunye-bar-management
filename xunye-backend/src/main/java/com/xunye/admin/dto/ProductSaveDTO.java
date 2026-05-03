package com.xunye.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品保存 DTO（新增/修改共用）
 */
@Data
public class ProductSaveDTO {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
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
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0", message = "售价不能小于0")
    private BigDecimal price;

    /**
     * 成本价
     */
    @DecimalMin(value = "0", message = "成本价不能小于0")
    private BigDecimal costPrice;

    /**
     * 当前库存
     */
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

    /**
     * 安全库存
     */
    @Min(value = 0, message = "安全库存不能小于0")
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

}
