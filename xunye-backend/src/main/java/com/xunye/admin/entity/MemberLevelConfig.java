package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("member_level_config")
public class MemberLevelConfig extends BaseEntity {

    private String level;

    private String name;

    private BigDecimal minAmount;

    private Integer upgradeOrders;

    private BigDecimal discount;

    private BigDecimal pointsRate;

    private String description;

    private String benefits;

    private String icon;

    private String color;

    private Integer sort;

    private Integer status;

}
