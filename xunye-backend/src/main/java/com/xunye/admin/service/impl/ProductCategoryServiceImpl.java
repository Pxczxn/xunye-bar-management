package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.ProductCategorySaveDTO;
import com.xunye.admin.entity.Product;
import com.xunye.admin.entity.ProductCategory;
import com.xunye.admin.mapper.ProductCategoryMapper;
import com.xunye.admin.mapper.ProductMapper;
import com.xunye.admin.service.ProductCategoryService;
import com.xunye.admin.vo.ProductCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<ProductCategoryVO> listCategories() {
        // 查询未删除的分类，按 sort 升序
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ProductCategory::getSort);
        List<ProductCategory> categories = categoryMapper.selectList(wrapper);

        // 转换为 VO
        return categories.stream().map(category -> {
            ProductCategoryVO vo = new ProductCategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void createCategory(ProductCategorySaveDTO dto) {
        // 检查分类名称是否已存在
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getName, dto.getName());
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类名称已存在");
        }

        // 创建分类
        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(dto, category);
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, ProductCategorySaveDTO dto) {
        // 检查分类是否存在
        ProductCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        // 检查名称是否与其他分类重复
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getName, dto.getName())
               .ne(ProductCategory::getId, id);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类名称已存在");
        }

        // 更新分类
        BeanUtils.copyProperties(dto, category);
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 检查分类是否存在
        ProductCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        // 检查该分类下是否有未删除的商品
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, id);
        long count = productMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该分类下还有商品，无法删除");
        }

        // 软删除（MyBatis-Plus 的 @TableLogic 会自动处理）
        categoryMapper.deleteById(id);
    }

}
