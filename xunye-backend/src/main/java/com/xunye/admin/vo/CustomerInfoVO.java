package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerInfoVO {

    private Long id;

    private String phone;

    private String nickname;

    private String avatar;

    private String memberLevel;

    private String memberLevelName;

    private BigDecimal points;

    private BigDecimal balance;

    private Integer totalOrders;

    private BigDecimal totalAmount;

    private LocalDateTime lastVisitAt;

    private LocalDateTime createdAt;

}
