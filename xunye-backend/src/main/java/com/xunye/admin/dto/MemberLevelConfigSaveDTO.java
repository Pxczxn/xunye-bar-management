package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class MemberLevelConfigSaveDTO {

    @NotBlank(message = "等级代码不能为空")
    private String level;

    @NotBlank(message = "等级名称不能为空")
    private String name;

    @NotNull(message = "最低消费金额不能为空")
    private BigDecimal minAmount;

    private Integer upgradeOrders;

    @NotNull(message = "折扣率不能为空")
    private BigDecimal discount;

    @NotNull(message = "积分倍率不能为空")
    private BigDecimal pointsRate;

    private String description;

    private Map<String, Object> benefits;

    private String icon;

    private String color;

    private Integer sort;

    private Integer status;

}
