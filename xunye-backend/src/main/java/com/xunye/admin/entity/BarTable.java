package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 桌台实体类
 */
@Data
@TableName("bar_table")
public class BarTable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 桌台名称
     */
    private String name;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 状态：EMPTY空闲、USING使用中、CLEANING清洁中、DISABLED停用
     */
    private String status;

    /**
     * 是否删除：0未删除，1已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
