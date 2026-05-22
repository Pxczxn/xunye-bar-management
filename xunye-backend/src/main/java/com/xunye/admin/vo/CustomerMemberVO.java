package com.xunye.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerMemberVO {

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

    private BigDecimal nextLevelAmount;

    private String nextLevelName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastVisitAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

}
