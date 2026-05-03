package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunye.admin.dto.ProductBrandSaveDTO;
import com.xunye.admin.entity.ProductBrand;
import com.xunye.admin.mapper.ProductBrandMapper;
import com.xunye.admin.service.ProductBrandService;
import com.xunye.admin.vo.ProductBrandVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品品牌历史 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class ProductBrandServiceImpl implements ProductBrandService {

    private final ProductBrandMapper brandMapper;

    @Override
    public List<ProductBrandVO> listBrands() {
        LambdaQueryWrapper<ProductBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductBrand::getDeleted, 0)
               .orderByAsc(ProductBrand::getSort)
               .orderByDesc(ProductBrand::getId);
        List<ProductBrand> brands = brandMapper.selectList(wrapper);

        return brands.stream().map(brand -> {
            ProductBrandVO vo = new ProductBrandVO();
            BeanUtils.copyProperties(brand, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ProductBrandVO createBrand(ProductBrandSaveDTO dto) {
        // 查询是否存在同名品牌（包括已删除的）
        LambdaQueryWrapper<ProductBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductBrand::getName, dto.getName());
        ProductBrand existing = brandMapper.selectOne(wrapper);

        if (existing != null) {
            if (existing.getDeleted() == 0) {
                // 品牌已存在且未删除，直接返回
                ProductBrandVO vo = new ProductBrandVO();
                BeanUtils.copyProperties(existing, vo);
                return vo;
            } else {
                // 品牌存在但已删除，恢复
                existing.setDeleted(0);
                if (dto.getSort() != null) {
                    existing.setSort(dto.getSort());
                }
                brandMapper.updateById(existing);
                ProductBrandVO vo = new ProductBrandVO();
                BeanUtils.copyProperties(existing, vo);
                return vo;
            }
        }

        // 新增品牌
        ProductBrand brand = new ProductBrand();
        BeanUtils.copyProperties(dto, brand);
        if (brand.getSort() == null) {
            brand.setSort(0);
        }
        brandMapper.insert(brand);

        ProductBrandVO vo = new ProductBrandVO();
        BeanUtils.copyProperties(brand, vo);
        return vo;
    }

    @Override
    public void deleteBrand(Long id) {
        // 软删除：将 deleted 设为 1
        ProductBrand brand = brandMapper.selectById(id);
        if (brand != null) {
            brand.setDeleted(1);
            brandMapper.updateById(brand);
        }
    }

    @Override
    public void autoSaveBrand(String brandName) {
        if (brandName == null || brandName.isBlank()) {
            return;
        }

        // 查询是否存在同名品牌（包括已删除的）
        LambdaQueryWrapper<ProductBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductBrand::getName, brandName.trim());
        ProductBrand existing = brandMapper.selectOne(wrapper);

        if (existing == null) {
            // 不存在，新增
            ProductBrand brand = new ProductBrand();
            brand.setName(brandName.trim());
            brand.setSort(0);
            brand.setDeleted(0);
            brandMapper.insert(brand);
        } else if (existing.getDeleted() == 1) {
            // 存在但已删除，恢复
            existing.setDeleted(0);
            brandMapper.updateById(existing);
        }
        // 存在且未删除，不做任何操作
    }

}
