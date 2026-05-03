package com.xunye.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付方式占比 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodVO {

    /**
     * 支付方式名称
     */
    private String method;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付占比
     */
    private Double percent;

}
