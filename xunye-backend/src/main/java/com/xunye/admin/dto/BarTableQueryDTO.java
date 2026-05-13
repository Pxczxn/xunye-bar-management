package com.xunye.admin.dto;

import lombok.Data;

/**
 * 桌台查询 DTO
 */
@Data
public class BarTableQueryDTO {

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页数量，默认10
     */
    private Integer pageSize = 10;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 状态：EMPTY、USING、CLEANING、DISABLED
     */
    private String status;

    /**
     * 关键词（桌台名称模糊搜索）
     */
    private String keyword;

}
