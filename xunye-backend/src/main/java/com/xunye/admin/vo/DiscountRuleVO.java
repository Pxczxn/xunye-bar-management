package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class DiscountRuleVO {

    private Long id;

    private String name;

    private String description;

    private String ruleType;

    private Integer priority;

    private Map<String, Object> conditions;

    private String exclusiveGroups;

    private Integer stackable;

    private BigDecimal maxDiscountAmount;

    private BigDecimal minPayAmount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
