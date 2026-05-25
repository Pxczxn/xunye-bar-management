package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.CouponTemplateSaveDTO;
import com.xunye.admin.entity.CouponTemplate;
import com.xunye.admin.mapper.CouponTemplateMapper;
import com.xunye.admin.service.CouponTemplateService;
import com.xunye.admin.util.EntityUtils;
import com.xunye.admin.vo.CouponTemplateVO;
import com.xunye.admin.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponTemplateServiceImpl implements CouponTemplateService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final CouponTemplateMapper couponTemplateMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<CouponTemplateVO> getPage(Integer pageNum, Integer pageSize, String keyword, String type, Integer status) {
        Page<CouponTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CouponTemplate::getName, keyword)
                    .or().like(CouponTemplate::getTitle, keyword));
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(CouponTemplate::getType, type);
        }
        if (status != null) {
            wrapper.eq(CouponTemplate::getStatus, status);
        }

        wrapper.orderByDesc(CouponTemplate::getSort)
                .orderByDesc(CouponTemplate::getCreatedAt);

        Page<CouponTemplate> result = couponTemplateMapper.selectPage(page, wrapper);

        List<CouponTemplateVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        PageResult<CouponTemplateVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(result.getTotal());
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);

        return pageResult;
    }

    @Override
    public CouponTemplateVO getDetail(Long id) {
        CouponTemplate template = EntityUtils.requireNonNull(
                couponTemplateMapper.selectById(id), "优惠券模板");
        return toVO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CouponTemplateSaveDTO dto) {
        validateDTO(dto);

        CouponTemplate template = new CouponTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setScopeConfig(writeJson(dto.getScopeConfig()));
        template.setIssueConfig(writeJson(dto.getIssueConfig()));
        template.setIssuedCount(0);
        template.setUsedCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        couponTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CouponTemplateSaveDTO dto) {
        CouponTemplate template = EntityUtils.requireNonNull(
                couponTemplateMapper.selectById(id), "优惠券模板");

        validateDTO(dto);

        BeanUtils.copyProperties(dto, template);
        template.setScopeConfig(writeJson(dto.getScopeConfig()));
        template.setIssueConfig(writeJson(dto.getIssueConfig()));
        template.setUpdatedAt(LocalDateTime.now());

        couponTemplateMapper.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CouponTemplate template = EntityUtils.requireNonNull(
                couponTemplateMapper.selectById(id), "优惠券模板");

        template.setDeleted(1);
        template.setUpdatedAt(LocalDateTime.now());
        couponTemplateMapper.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        CouponTemplate template = EntityUtils.requireNonNull(
                couponTemplateMapper.selectById(id), "优惠券模板");

        if (status != 0 && status != 1) {
            throw new BusinessException("状态值无效");
        }

        template.setStatus(status);
        template.setUpdatedAt(LocalDateTime.now());
        couponTemplateMapper.updateById(template);
    }

    private void validateDTO(CouponTemplateSaveDTO dto) {
        if ("AMOUNT".equals(dto.getType())) {
            if (dto.getDiscountAmount() == null || dto.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BusinessException("满减券必须设置优惠金额");
            }
        } else if ("DISCOUNT".equals(dto.getType())) {
            if (dto.getDiscountRate() == null || dto.getDiscountRate().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BusinessException("折扣券必须设置折扣率");
            }
        } else {
            throw new BusinessException("不支持的优惠券类型");
        }
    }

    private CouponTemplateVO toVO(CouponTemplate template) {
        CouponTemplateVO vo = new CouponTemplateVO();
        BeanUtils.copyProperties(template, vo);
        vo.setScopeConfig(readJson(template.getScopeConfig()));
        vo.setIssueConfig(readJson(template.getIssueConfig()));
        return vo;
    }

    private Map<String, Object> readJson(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private String writeJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new BusinessException("配置数据格式错误");
        }
    }

}
