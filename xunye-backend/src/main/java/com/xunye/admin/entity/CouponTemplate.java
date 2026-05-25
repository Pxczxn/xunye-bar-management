package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("coupon_template")
public class CouponTemplate extends BaseEntity {

    private String name;

    private String title;

    private String description;

    private String type;

    private BigDecimal discountAmount;

    private BigDecimal discountRate;

    private BigDecimal minAmount;

    private String scopeType;

    private String scopeConfig;

    private String issueType;

    private String issueConfig;

    private Integer validDays;

    private Integer maxUseCount;

    private Integer totalCount;

    private Integer issuedCount;

    private Integer usedCount;

    private String memberLevelLimit;

    private Integer status;

    private Integer sort;

}
