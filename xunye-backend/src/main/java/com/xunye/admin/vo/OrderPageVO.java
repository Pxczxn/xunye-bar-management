package com.xunye.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单分页查询 VO
 */
@Data
public class OrderPageVO {

    /**
     * 订单ID
     */
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime paidAt;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime cancelledAt;

    /**
     * 订单项列表
     */
    private List<OrderItemVO> items;

}
