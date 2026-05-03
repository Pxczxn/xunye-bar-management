package com.xunye.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryRecordVO {

    private Long id;

    private Long productId;

    private String productName;

    private String type;

    private String typeText;

    private Integer changeQuantity;

    private Integer beforeStock;

    private Integer afterStock;

    private String reason;

    private String operatorName;

    private LocalDateTime createdAt;
}
