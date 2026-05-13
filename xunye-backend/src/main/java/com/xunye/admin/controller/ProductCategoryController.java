package com.xunye.admin.controller;

import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.ProductCategorySaveDTO;
import com.xunye.admin.service.ProductCategoryService;
import com.xunye.admin.vo.ProductCategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类接口控制器
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER"})
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    /**
     * 查询分类列表
     */
    @GetMapping
    public ApiResponse<List<ProductCategoryVO>> listCategories() {
        return ApiResponse.success(categoryService.listCategories());
    }

    /**
     * 新增分类
     */
    @PostMapping
    public ApiResponse<Void> createCategory(@Valid @RequestBody ProductCategorySaveDTO dto) {
        categoryService.createCategory(dto);
        return ApiResponse.success();
    }

    /**
     * 修改分类
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody ProductCategorySaveDTO dto) {
        categoryService.updateCategory(id, dto);
        return ApiResponse.success();
    }

    /**
     * 删除分类（软删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success();
    }

}
