package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("order_info")
public class OrderInfo {

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 桌台ID
     */
    private Long tableId;

    /**
     * 桌台名称
     */
    private String tableName;

    /**
     * 顾客ID
     */
    private Long customerId;

    /**
     * 顾客手机号
     */
    private String customerPhone;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    private BigDecimal originalAmount;

    private BigDecimal discountAmount;

    private Long couponId;

    /**
     * 订单状态：UNPAID未支付、PAID已支付、CANCELLED已取消
     */
    private String status;

    /**
     * 履约状态：PENDING待处理、MAKING制作中、FINISHED已完成
     */
    private String serveStatus;

    /**
     * 支付方式：WECHAT微信、ALIPAY支付宝、CASH现金
     */
    private String paymentMethod;

    /**
     * 订单来源：ADMIN_POS吧台点单、CUSTOMER_MINI顾客扫码
     */
    private String source;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 取消时间
     */
    private LocalDateTime cancelledAt;

    /**
     * 删除标志：0未删除，1已删除
     */
    @TableLogic
    private Integer deleted;

}
