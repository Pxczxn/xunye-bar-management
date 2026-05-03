package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 仪表盘概要数据 VO
 */
@Data
public class DashboardSummaryVO {

    /**
     * 今日营收
     */
    private BigDecimal todayRevenue;

    /**
     * 今日订单数
     */
    private Integer todayOrderCount;

    /**
     * 平均客单价
     */
    private BigDecimal averageOrderValue;

    /**
     * 库存预警数
     */
    private Integer inventoryWarningCount;

}
