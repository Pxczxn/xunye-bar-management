package com.xunye.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 桌台区域实体类
 */
@Data
@TableName("table_area")
public class TableArea {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：1启用，0禁用
     */
    private Integer status;

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
