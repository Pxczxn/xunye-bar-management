package com.xunye.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 桌台 VO
 */
@Data
public class BarTableVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

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
     * 创建时间
     */
    private LocalDateTime createdAt;

}
