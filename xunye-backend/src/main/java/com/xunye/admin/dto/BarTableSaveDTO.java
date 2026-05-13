package com.xunye.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 桌台保存 DTO（新增/修改共用）
 */
@Data
public class BarTableSaveDTO {

    /**
     * 区域ID
     */
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    /**
     * 桌台名称
     */
    @NotBlank(message = "桌台名称不能为空")
    private String name;

    /**
     * 容纳人数
     */
    @Min(value = 1, message = "容纳人数不能小于1")
    private Integer capacity = 1;

    /**
     * 状态：EMPTY、USING、CLEANING、DISABLED
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(EMPTY|USING|CLEANING|DISABLED)$", message = "状态只能是 EMPTY、USING、CLEANING 或 DISABLED")
    private String status;

}
