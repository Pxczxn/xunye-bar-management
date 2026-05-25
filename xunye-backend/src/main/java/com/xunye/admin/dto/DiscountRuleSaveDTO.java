package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class DiscountRuleSaveDTO {

    @NotBlank(message = "规则名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    @NotNull(message = "优先级不能为空")
    private Integer priority;

    private Map<String, Object> conditions;

    private String exclusiveGroups;

    private Integer stackable;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minPayAmount;

    private Integer status;

}
