package com.xunye.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class CouponTemplateSaveDTO {

    @NotBlank(message = "优惠券名称不能为空")
    private String name;

    @NotBlank(message = "优惠券标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "优惠券类型不能为空")
    private String type;

    private BigDecimal discountAmount;

    private BigDecimal discountRate;

    @NotNull(message = "最低使用金额不能为空")
    private BigDecimal minAmount;

    @NotBlank(message = "适用范围类型不能为空")
    private String scopeType;

    private Map<String, Object> scopeConfig;

    @NotBlank(message = "发放类型不能为空")
    private String issueType;

    private Map<String, Object> issueConfig;

    @NotNull(message = "有效天数不能为空")
    private Integer validDays;

    private Integer maxUseCount;

    private Integer totalCount;

    private String memberLevelLimit;

    private Integer status;

    private Integer sort;

}
