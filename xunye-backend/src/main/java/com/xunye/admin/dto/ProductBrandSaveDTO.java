package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 品牌保存 DTO
 */
@Data
public class ProductBrandSaveDTO {

    /**
     * 品牌名称
     */
    @NotBlank(message = "品牌名称不能为空")
    private String name;

    /**
     * 排序
     */
    private Integer sort;

}
