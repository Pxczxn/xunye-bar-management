package com.xunye.admin.controller;

import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.InventoryAdjustDTO;
import com.xunye.admin.service.InventoryService;
import com.xunye.admin.vo.InventoryRecordsPageVO;
import com.xunye.admin.vo.InventoryWarningVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/warnings")
    public ApiResponse<List<InventoryWarningVO>> getWarnings() {
        return ApiResponse.success(inventoryService.getWarnings());
    }

    @GetMapping("/records")
    public ApiResponse<InventoryRecordsPageVO> getRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String type) {
        return ApiResponse.success(inventoryService.getRecords(pageNum, pageSize, productName, type));
    }

    @PostMapping("/adjust")
    public ApiResponse<Void> adjust(@Valid @RequestBody InventoryAdjustDTO dto) {
        inventoryService.adjust(dto);
        return ApiResponse.success("库存调整成功", null);
    }
}
