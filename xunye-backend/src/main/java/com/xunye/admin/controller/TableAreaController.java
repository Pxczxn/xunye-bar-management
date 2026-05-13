package com.xunye.admin.controller;

import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.TableAreaSaveDTO;
import com.xunye.admin.service.TableAreaService;
import com.xunye.admin.vo.TableAreaVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 桌台区域控制器
 */
@RestController
@RequestMapping("/api/admin/table-areas")
@RequiredArgsConstructor
public class TableAreaController {

    private final TableAreaService tableAreaService;

    /**
     * 查询区域列表
     */
    @GetMapping
    @RequireRole({"BOSS", "MANAGER", "STAFF"})
    public ApiResponse<List<TableAreaVO>> listAreas() {
        return ApiResponse.success(tableAreaService.listAreas());
    }

    /**
     * 新增区域
     */
    @PostMapping
    @RequireRole({"BOSS", "MANAGER"})
    public ApiResponse<Void> createArea(@Valid @RequestBody TableAreaSaveDTO dto) {
        tableAreaService.createArea(dto);
        return ApiResponse.success();
    }

    /**
     * 修改区域
     */
    @PutMapping("/{id}")
    @RequireRole({"BOSS", "MANAGER"})
    public ApiResponse<Void> updateArea(@PathVariable Long id, @Valid @RequestBody TableAreaSaveDTO dto) {
        tableAreaService.updateArea(id, dto);
        return ApiResponse.success();
    }

    /**
     * 删除区域
     */
    @DeleteMapping("/{id}")
    @RequireRole({"BOSS", "MANAGER"})
    public ApiResponse<Void> deleteArea(@PathVariable Long id) {
        tableAreaService.deleteArea(id);
        return ApiResponse.success();
    }

}
