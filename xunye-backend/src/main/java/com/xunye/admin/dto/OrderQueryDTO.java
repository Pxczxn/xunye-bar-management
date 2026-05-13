package com.xunye.admin.dto;

import lombok.Data;

/**
 * 订单查询 DTO
 */
@Data
public class OrderQueryDTO {

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页数量，默认10
     */
    private Integer pageSize = 10;

    /**
     * 订单编号（模糊查询）
     */
    private String orderNo;

    /**
     * 桌台名称（模糊查询）
     */
    private String tableName;

    /**
     * 订单状态：UNPAID、PAID、CANCELLED
     */
    private String status;

    /**
     * 履约状态：PENDING、MAKING、FINISHED
     */
    private String serveStatus;

    /**
     * 订单来源：ADMIN_POS、CUSTOMER_MINI
     */
    private String source;

    /**
     * 排除的订单状态（ne 条件）
     */
    private String excludeStatus;

}
