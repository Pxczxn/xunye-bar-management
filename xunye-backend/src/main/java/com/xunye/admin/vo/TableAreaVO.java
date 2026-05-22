package com.xunye.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 桌台区域 VO
 */
@Data
public class TableAreaVO {

    /**
     * 主键ID
     */
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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

}
