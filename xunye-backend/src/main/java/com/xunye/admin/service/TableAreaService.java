package com.xunye.admin.service;

import com.xunye.admin.dto.TableAreaSaveDTO;
import com.xunye.admin.vo.TableAreaVO;

import java.util.List;

/**
 * 桌台区域 Service 接口
 */
public interface TableAreaService {

    /**
     * 查询区域列表
     */
    List<TableAreaVO> listAreas();

    /**
     * 新增区域
     */
    void createArea(TableAreaSaveDTO dto);

    /**
     * 修改区域
     */
    void updateArea(Long id, TableAreaSaveDTO dto);

    /**
     * 删除区域
     */
    void deleteArea(Long id);

}
