package com.xunye.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 桌台区域保存 DTO（新增/修改共用）
 */
@Data
public class TableAreaSaveDTO {

    /**
     * 区域名称
     */
    @NotBlank(message = "区域名称不能为空")
    private String name;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序不能小于0")
    private Integer sort;

    /**
     * 状态：1启用，0禁用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

}
