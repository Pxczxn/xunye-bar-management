package com.xunye.admin.service;

import com.xunye.admin.dto.ProductBrandSaveDTO;
import com.xunye.admin.vo.ProductBrandVO;

import java.util.List;

/**
 * 商品品牌历史 Service 接口
 */
public interface ProductBrandService {

    /**
     * 查询品牌列表
     */
    List<ProductBrandVO> listBrands();

    /**
     * 新增品牌
     */
    ProductBrandVO createBrand(ProductBrandSaveDTO dto);

    /**
     * 删除品牌（软删除）
     */
    void deleteBrand(Long id);

    /**
     * 自动保存品牌历史（商品新增/编辑时调用）
     * 如果品牌不存在则新增，如果存在但已删除则恢复
     */
    void autoSaveBrand(String brandName);

}
