package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 订单支付 DTO
 */
@Data
public class OrderPayDTO {

    /**
     * 支付方式：WECHAT、ALIPAY、CASH
     */
    @NotBlank(message = "支付方式不能为空")
    @Pattern(regexp = "^(WECHAT|ALIPAY|CASH)$", message = "支付方式只能是 WECHAT、ALIPAY 或 CASH")
    private String paymentMethod;

}
