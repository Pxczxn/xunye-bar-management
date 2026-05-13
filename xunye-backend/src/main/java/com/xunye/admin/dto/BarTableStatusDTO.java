package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 桌台状态修改 DTO
 */
@Data
public class BarTableStatusDTO {

    /**
     * 状态：EMPTY、USING、CLEANING、DISABLED
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(EMPTY|USING|CLEANING|DISABLED)$", message = "状态只能是 EMPTY、USING、CLEANING 或 DISABLED")
    private String status;

}
