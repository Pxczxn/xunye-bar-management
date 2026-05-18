package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerStatsVO {

    private BigDecimal points;

    private Integer coupons;

    private Integer totalOrders;

    private BigDecimal totalAmount;

}
