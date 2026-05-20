package com.xunye.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单 DTO
 */
@Data
public class OrderCreateDTO {

    /**
     * 桌台ID
     */
    @NotNull(message = "桌台ID不能为空")
    private Long tableId;

    /**
     * 顾客手机号
     */
    private String phone;

    /**
     * 使用的优惠券ID
     */
    private Long couponId;

    /**
     * 订单项列表
     */
    @NotEmpty(message = "订单项不能为空")
    @Valid
    private List<OrderItemDTO> items;

    /**
     * 订单备注
     */
    private String remark;

}
