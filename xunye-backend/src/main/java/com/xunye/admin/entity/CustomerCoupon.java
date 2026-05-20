package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("customer_coupon")
public class CustomerCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String title;

    private String ruleText;

    private BigDecimal discountAmount;

    private BigDecimal minAmount;

    private Integer used;

    private LocalDate validUntil;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;

}
