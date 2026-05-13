package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项 VO
 */
@Data
public class OrderItemVO {

    /**
     * 订单项ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 商品金额
     */
    private BigDecimal amount;

}
