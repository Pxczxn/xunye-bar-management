package com.xunye.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 最近订单 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRecentVO {

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 桌号
     */
    private String tableName;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 状态：PAID-已支付，UNPAID-未支付，CANCELLED-已取消
     */
    private String status;

    /**
     * 创建时间
     */
    private String createdAt;

}
