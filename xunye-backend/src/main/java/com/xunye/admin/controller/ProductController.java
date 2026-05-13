package com.xunye.admin.controller;

import com.xunye.admin.annotation.RequireRole;
import com.xunye.admin.common.ApiResponse;
import com.xunye.admin.dto.ProductQueryDTO;
import com.xunye.admin.dto.ProductSaveDTO;
import com.xunye.admin.dto.ProductStatusDTO;
import com.xunye.admin.service.ProductService;
import com.xunye.admin.vo.PageResult;
import com.xunye.admin.vo.ProductPageVO;
import com.xunye.admin.vo.ProductSimpleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口控制器
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER"})
public class ProductController {

    private final ProductService productService;

    /**
     * 获取商品简表（已改为从数据库查询）
     */
    @GetMapping("/simple")
    public ApiResponse<List<ProductSimpleVO>> getSimpleList() {
        return ApiResponse.success(productService.getSimpleList());
    }

    /**
     * 商品分页查询
     */
    @GetMapping
    public ApiResponse<PageResult<ProductPageVO>> getProductPage(ProductQueryDTO queryDTO) {
        return ApiResponse.success(productService.getProductPage(queryDTO));
    }

    /**
     * 查询商品详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductPageVO> getProductDetail(@PathVariable Long id) {
        return ApiResponse.success(productService.getProductDetail(id));
    }

    /**
     * 新增商品
     */
    @PostMapping
    public ApiResponse<Void> createProduct(@Valid @RequestBody ProductSaveDTO dto) {
        productService.createProduct(dto);
        return ApiResponse.success();
    }

    /**
     * 修改商品
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        productService.updateProduct(id, dto);
        return ApiResponse.success();
    }

    /**
     * 修改商品上下架状态
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateProductStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusDTO dto) {
        productService.updateProductStatus(id, dto);
        return ApiResponse.success();
    }

    /**
     * 删除商品（软删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success();
    }

}
