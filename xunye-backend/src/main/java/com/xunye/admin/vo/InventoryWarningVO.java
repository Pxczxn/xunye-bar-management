package com.xunye.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预警 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryWarningVO {

    private Long productId;

    private String productName;

    private Integer currentStock;

    /**
     * 安全库存
     */
    private Integer safeStock;

    /**
     * 单位
     */
    private String unit;

    /**
     * 预警级别：HIGH-高，MEDIUM-中，LOW-低
     */
    private String warningLevel;

}
