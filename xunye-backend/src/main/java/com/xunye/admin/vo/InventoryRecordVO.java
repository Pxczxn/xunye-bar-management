package com.xunye.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
