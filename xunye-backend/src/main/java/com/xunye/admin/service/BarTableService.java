package com.xunye.admin.service;

import com.xunye.admin.dto.BarTableQueryDTO;
import com.xunye.admin.dto.BarTableSaveDTO;
import com.xunye.admin.dto.BarTableStatusDTO;
import com.xunye.admin.vo.BarTableVO;
import com.xunye.admin.vo.PageResult;

/**
 * 桌台 Service 接口
 */
public interface BarTableService {

    /**
     * 桌台分页查询
     */
    PageResult<BarTableVO> getTablePage(BarTableQueryDTO queryDTO);

    /**
     * 新增桌台
     */
    void createTable(BarTableSaveDTO dto);

    /**
     * 修改桌台
     */
    void updateTable(Long id, BarTableSaveDTO dto);

    /**
     * 修改桌台状态
     */
    void updateTableStatus(Long id, BarTableStatusDTO dto);

    /**
     * 清台：仅在桌台没有待处理/制作中的有效订单时允许置为空闲
     */
    void clearTable(Long id);

    /**
     * 删除桌台
     */
    void deleteTable(Long id);

}
