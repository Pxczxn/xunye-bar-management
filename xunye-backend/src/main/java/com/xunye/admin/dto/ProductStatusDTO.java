package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 商品状态修改 DTO
 */
@Data
public class ProductStatusDTO {

    /**
     * 状态：ON_SALE / OFF_SALE
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(ON_SALE|OFF_SALE)$", message = "状态只能是 ON_SALE 或 OFF_SALE")
    private String status;

}
