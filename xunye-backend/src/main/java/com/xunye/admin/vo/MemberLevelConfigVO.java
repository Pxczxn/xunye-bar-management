package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class MemberLevelConfigVO {

    private Long id;

    private String level;

    private String name;

    private BigDecimal minAmount;

    private Integer upgradeOrders;

    private BigDecimal discount;

    private BigDecimal pointsRate;

    private String description;

    private Map<String, Object> benefits;

    private String icon;

    private String color;

    private Integer sort;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
