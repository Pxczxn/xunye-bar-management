package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerOrderSubmitVO {

    private String orderNo;

    private BigDecimal totalAmount;

    private BigDecimal originalAmount;

    private BigDecimal discountAmount;

    private String status;

}
