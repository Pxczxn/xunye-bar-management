package com.xunye.admin.service;

import com.xunye.admin.dto.ProductQueryDTO;
import com.xunye.admin.dto.ProductSaveDTO;
import com.xunye.admin.dto.ProductStatusDTO;
import com.xunye.admin.vo.PageResult;
import com.xunye.admin.vo.ProductPageVO;
import com.xunye.admin.vo.ProductSimpleVO;

import java.util.List;

/**
 * 商品 Service 接口
 */
public interface ProductService {

    /**
     * 获取商品简表
     */
    List<ProductSimpleVO> getSimpleList();

    /**
     * 商品分页查询
     */
    PageResult<ProductPageVO> getProductPage(ProductQueryDTO queryDTO);

    /**
     * 查询商品详情
     */
    ProductPageVO getProductDetail(Long id);

    /**
     * 新增商品
     */
    void createProduct(ProductSaveDTO dto);

    /**
     * 修改商品
     */
    void updateProduct(Long id, ProductSaveDTO dto);

    /**
     * 修改商品上下架状态
     */
    void updateProductStatus(Long id, ProductStatusDTO dto);

    /**
     * 删除商品（软删除）
     */
    void deleteProduct(Long id);

}
