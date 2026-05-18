package com.xunye.admin.controller;

import com.xunye.admin.annotation.AuditLog;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 商品接口控制器
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@RequireRole({"BOSS", "MANAGER"})
public class ProductController {

    private final ProductService productService;

    @Value("${file.upload.base-path}")
    private String fileUploadBasePath;

    @Value("${file.upload.product-path}")
    private String productPath;

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
    @AuditLog(operation = "新增商品", module = "商品管理")
    public ApiResponse<Void> createProduct(@Valid @RequestBody ProductSaveDTO dto) {
        productService.createProduct(dto);
        return ApiResponse.success();
    }

    /**
     * 修改商品
     */
    @PutMapping("/{id}")
    @AuditLog(operation = "修改商品", module = "商品管理")
    public ApiResponse<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        productService.updateProduct(id, dto);
        return ApiResponse.success();
    }

    /**
     * 修改商品上下架状态
     */
    @PatchMapping("/{id}/status")
    @AuditLog(operation = "修改商品状态", module = "商品管理")
    public ApiResponse<Void> updateProductStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusDTO dto) {
        productService.updateProductStatus(id, dto);
        return ApiResponse.success();
    }

    /**
     * 删除商品（软删除）
     */
    @DeleteMapping("/{id}")
    @AuditLog(operation = "删除商品", module = "商品管理")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success();
    }

    /**
     * 上传商品图片
     */
    @PostMapping("/upload-image")
    @AuditLog(operation = "上传商品图片", module = "商品管理")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error("只能上传图片文件");
        }

        // 验证文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return ApiResponse.error("文件大小不能超过10MB");
        }

        try {
            // 获取原始文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + extension;

            // 确保目录存在
            Path uploadPath = Paths.get(fileUploadBasePath + productPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // 返回相对路径（用于存储到数据库）
            String imageUrl = "/images" + productPath + "/" + filename;
            return ApiResponse.success(imageUrl);

        } catch (IOException e) {
            return ApiResponse.error("文件上传失败：" + e.getMessage());
        }
    }

}
