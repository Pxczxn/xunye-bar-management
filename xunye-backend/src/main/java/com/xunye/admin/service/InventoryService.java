package com.xunye.admin.service;

import com.xunye.admin.dto.InventoryAdjustDTO;
import com.xunye.admin.vo.InventoryRecordsPageVO;
import com.xunye.admin.vo.InventoryWarningVO;

import java.util.List;

public interface InventoryService {

    List<InventoryWarningVO> getWarnings();

    InventoryRecordsPageVO getRecords(Integer pageNum, Integer pageSize, String productName, String type);

    void adjust(InventoryAdjustDTO dto);
}
