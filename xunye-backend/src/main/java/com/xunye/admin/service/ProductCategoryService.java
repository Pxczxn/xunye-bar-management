package com.xunye.admin.service;

import com.xunye.admin.dto.ProductCategorySaveDTO;
import com.xunye.admin.vo.ProductCategoryVO;

import java.util.List;

/**
 * 商品分类 Service 接口
 */
public interface ProductCategoryService {

    /**
     * 查询分类列表
     */
    List<ProductCategoryVO> listCategories();

    /**
     * 新增分类
     */
    void createCategory(ProductCategorySaveDTO dto);

    /**
     * 修改分类
     */
    void updateCategory(Long id, ProductCategorySaveDTO dto);

    /**
     * 删除分类（软删除）
     */
    void deleteCategory(Long id);

}
