package com.xunye.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class InventoryAdjustDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "操作类型不能为空")
    private String type;

    @NotNull(message = "数量不能为空")
    @PositiveOrZero(message = "数量不能为负数")
    private Integer quantity;

    private String reason;
}
