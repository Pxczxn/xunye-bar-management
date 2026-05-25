package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("discount_rule")
public class DiscountRule extends BaseEntity {

    private String name;

    private String description;

    private String ruleType;

    private Integer priority;

    private String conditions;

    private String exclusiveGroups;

    private Integer stackable;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minPayAmount;

    private Integer status;

}
