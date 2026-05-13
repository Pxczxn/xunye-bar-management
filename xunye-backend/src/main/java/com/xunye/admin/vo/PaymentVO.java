package com.xunye.admin.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentVO {
    private String paymentNo;
    private String orderNo;
    private BigDecimal amount;
    private String provider;
    private String status;
}
