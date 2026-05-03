package com.xunye.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售趋势 VO（最近7天）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesTrendVO {

    /**
     * 日期（MM-dd格式）
     */
    private String date;

    /**
     * 营收金额
     */
    private BigDecimal revenue;

    /**
     * 订单数量
     */
    private Integer orderCount;

}
