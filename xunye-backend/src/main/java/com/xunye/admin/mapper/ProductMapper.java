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

    /**
     * 原子性扣减库存（使用数据库锁）
     * @return 影响的行数，0表示库存不足或商品不存在
     */
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 原子性增加库存（使用数据库锁）
     * @return 影响的行数
     */
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 原子性调整库存，delta 为正数入库，负数出库，保证 stock + delta >= 0
     * @return 影响行数，0 表示库存不足
     */
    int adjustStock(@Param("productId") Long productId, @Param("delta") int delta);

    /**
     * 原子性设置库存（盘点）
     */
    int setStock(@Param("productId") Long productId, @Param("stock") int stock);

}
