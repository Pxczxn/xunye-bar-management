package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.ProductQueryDTO;
import com.xunye.admin.dto.ProductSaveDTO;
import com.xunye.admin.dto.ProductStatusDTO;
import com.xunye.admin.entity.Product;
import com.xunye.admin.entity.ProductCategory;
import com.xunye.admin.enums.ProductStatus;
import com.xunye.admin.mapper.ProductCategoryMapper;
import com.xunye.admin.mapper.ProductMapper;
import com.xunye.admin.service.ProductBrandService;
import com.xunye.admin.service.ProductService;
import com.xunye.admin.vo.PageResult;
import com.xunye.admin.vo.ProductPageVO;
import com.xunye.admin.vo.ProductSimpleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductBrandService brandService;

    @Override
    public List<ProductSimpleVO> getSimpleList() {
        // 联表查询：商品 + 分类名称
        List<ProductSimpleVO> list = productMapper.selectProductPage(null, null, null, 0, 500)
                .stream()
                .map(vo -> {
                    ProductSimpleVO simple = new ProductSimpleVO();
                    simple.setId(vo.getId());
                    simple.setName(vo.getName());
                    simple.setCategory(vo.getCategoryName());
                    simple.setBrand(vo.getBrand());
                    simple.setSpec(vo.getSpec());
                    simple.setPrice(vo.getPrice());
                    simple.setStock(vo.getStock());
                    simple.setSafeStock(vo.getSafeStock());
                    simple.setStatus(vo.getStatus());
                    return simple;
                })
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public PageResult<ProductPageVO> getProductPage(ProductQueryDTO queryDTO) {
        // 计算偏移量
        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10;
        int offset = (pageNum - 1) * pageSize;

        // 查询数据
        List<ProductPageVO> records = productMapper.selectProductPage(
                queryDTO.getKeyword(),
                queryDTO.getCategoryId(),
                queryDTO.getStatus(),
                offset,
                pageSize
        );

        // 查询总数
        int total = productMapper.selectProductCount(
                queryDTO.getKeyword(),
                queryDTO.getCategoryId(),
                queryDTO.getStatus()
        );

        return new PageResult<>(records, (long) total, pageNum, pageSize);
    }

    @Override
    public ProductPageVO getProductDetail(Long id) {
        // 查询商品
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 转换为 VO
        ProductPageVO vo = new ProductPageVO();
        BeanUtils.copyProperties(product, vo);

        // 查询分类名称
        ProductCategory category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        return vo;
    }

    @Override
    public void createProduct(ProductSaveDTO dto) {
        ProductCategory category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        if (product.getStock() == null) {
            product.setStock(0);
        }
        if (product.getSafeStock() == null) {
            product.setSafeStock(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ON_SALE.getCode());
        }
        productMapper.insert(product);

        brandService.autoSaveBrand(dto.getBrand());
    }

    @Override
    public void updateProduct(Long id, ProductSaveDTO dto) {
        // 检查商品是否存在
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 检查分类是否存在
        ProductCategory category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        // 更新商品
        BeanUtils.copyProperties(dto, product);
        productMapper.updateById(product);

        // 自动保存品牌历史
        brandService.autoSaveBrand(dto.getBrand());
    }

    @Override
    public void updateProductStatus(Long id, ProductStatusDTO dto) {
        // 检查商品是否存在
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 更新状态
        product.setStatus(dto.getStatus());
        productMapper.updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        // 检查商品是否存在
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 软删除（MyBatis-Plus 的 @TableLogic 会自动处理）
        productMapper.deleteById(id);
    }

}
