package com.xunye.admin.controller;

import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.ProductBrandSaveDTO;
import com.xunye.admin.service.ProductBrandService;
import com.xunye.admin.vo.ProductBrandVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品品牌历史接口控制器
 */
@RestController
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER"})
public class ProductBrandController {

    private final ProductBrandService brandService;

    /**
     * 查询品牌列表
     */
    @GetMapping
    public ApiResponse<List<ProductBrandVO>> listBrands() {
        return ApiResponse.success(brandService.listBrands());
    }

    /**
     * 新增品牌
     */
    @PostMapping
    public ApiResponse<ProductBrandVO> createBrand(@Valid @RequestBody ProductBrandSaveDTO dto) {
        return ApiResponse.success(brandService.createBrand(dto));
    }

    /**
     * 删除品牌（软删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ApiResponse.success();
    }

}
