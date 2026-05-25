package com.xunye.admin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class CouponTemplateVO {

    private Long id;

    private String name;

    private String title;

    private String description;

    private String type;

    private BigDecimal discountAmount;

    private BigDecimal discountRate;

    private BigDecimal minAmount;

    private String scopeType;

    private Map<String, Object> scopeConfig;

    private String issueType;

    private Map<String, Object> issueConfig;

    private Integer validDays;

    private Integer maxUseCount;

    private Integer totalCount;

    private Integer issuedCount;

    private Integer usedCount;

    private String memberLevelLimit;

    private Integer status;

    private Integer sort;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
