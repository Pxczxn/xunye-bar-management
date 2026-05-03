package com.xunye.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunye.admin.entity.Product;
import com.xunye.admin.vo.ProductPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品 Mapper 接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 分页查询商品（联表查询分类名称）
     */
    List<ProductPageVO> selectProductPage(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    /**
     * 查询商品总数（用于分页）
     */
    int selectProductCount(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") String status
    );

}
