package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水实体类
 */
@Data
@TableName("inventory_record")
public class InventoryRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String productName;

    private String type;

    private Integer changeQuantity;

    private Integer beforeStock;

    private Integer afterStock;

    private String reason;

    private String operatorName;

    private LocalDateTime createdAt;

}
