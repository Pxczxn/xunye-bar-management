package com.xunye.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerInfoVO {

    private Long id;

    private String customerNo;

    private String openid;

    private String phone;

    private String nickname;

    private String avatar;

    private LocalDate birthday;

    private String gender;

    private String favoriteTaste;

    private String favoriteTable;

    private String memberLevel;

    private String memberLevelName;

    private BigDecimal points;

    private BigDecimal balance;

    private Integer totalOrders;

    private BigDecimal totalAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastVisitAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

}
