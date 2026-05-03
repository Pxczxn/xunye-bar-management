package com.xunye.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRecordsPageVO {

    private List<InventoryRecordVO> records;

    private Long total;

    private Integer pageNum;

    private Integer pageSize;
}
