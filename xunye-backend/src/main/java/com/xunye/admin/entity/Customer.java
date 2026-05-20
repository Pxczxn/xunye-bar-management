package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("customer")
public class Customer {

    @TableId(type = IdType.AUTO)
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

    private BigDecimal points;

    private BigDecimal balance;

    private Integer totalOrders;

    private BigDecimal totalAmount;

    private LocalDateTime lastVisitAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
