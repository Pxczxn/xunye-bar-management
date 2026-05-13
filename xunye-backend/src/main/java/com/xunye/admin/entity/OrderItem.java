package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体类
 */
@Data
@TableName("order_item")
public class OrderItem {

    /**
     * 订单项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单ID
     */
    private Long orderId;

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

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
