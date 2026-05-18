package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberLevelVO {

    private String level;

    private String name;

    private BigDecimal minAmount;

    private BigDecimal discount;

    private BigDecimal pointsRate;

    private String description;

    private Integer sort;
}
