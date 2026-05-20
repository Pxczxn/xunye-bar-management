package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerCouponVO {

    private Long id;

    private String title;

    private String rule;

    private BigDecimal discountAmount;

    private BigDecimal minAmount;

    private Boolean used;

    private LocalDate validUntil;

}
