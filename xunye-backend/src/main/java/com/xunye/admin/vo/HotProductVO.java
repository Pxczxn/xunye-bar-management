package com.xunye.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 热销商品 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotProductVO {

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 销售数量
     */
    private Integer salesCount;

    /**
     * 销售金额
     */
    private BigDecimal salesAmount;

}
