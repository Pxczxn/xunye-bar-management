package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.DiscountRuleSaveDTO;
import com.xunye.admin.entity.DiscountRule;
import com.xunye.admin.mapper.DiscountRuleMapper;
import com.xunye.admin.service.DiscountRuleService;
import com.xunye.admin.util.EntityUtils;
import com.xunye.admin.vo.DiscountRuleVO;
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
public class DiscountRuleServiceImpl implements DiscountRuleService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final DiscountRuleMapper discountRuleMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<DiscountRuleVO> getPage(Integer pageNum, Integer pageSize, String keyword, String ruleType, Integer status) {
        Page<DiscountRule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DiscountRule> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(DiscountRule::getName, keyword)
                    .or().like(DiscountRule::getDescription, keyword));
        }
        if (StringUtils.hasText(ruleType)) {
            wrapper.eq(DiscountRule::getRuleType, ruleType);
        }
        if (status != null) {
            wrapper.eq(DiscountRule::getStatus, status);
        }

        wrapper.orderByDesc(DiscountRule::getPriority)
                .orderByDesc(DiscountRule::getCreatedAt);

        Page<DiscountRule> result = discountRuleMapper.selectPage(page, wrapper);

        List<DiscountRuleVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        PageResult<DiscountRuleVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(result.getTotal());
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);

        return pageResult;
    }

    @Override
    public List<DiscountRuleVO> listAll() {
        LambdaQueryWrapper<DiscountRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiscountRule::getStatus, 1)
                .orderByDesc(DiscountRule::getPriority);

        return discountRuleMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public DiscountRuleVO getDetail(Long id) {
        DiscountRule rule = EntityUtils.requireNonNull(
                discountRuleMapper.selectById(id), "折扣规则");
        return toVO(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DiscountRuleSaveDTO dto) {
        validateDTO(dto);

        DiscountRule rule = new DiscountRule();
        BeanUtils.copyProperties(dto, rule);
        rule.setConditions(writeConditions(dto.getConditions()));
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());

        discountRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DiscountRuleSaveDTO dto) {
        DiscountRule rule = EntityUtils.requireNonNull(
                discountRuleMapper.selectById(id), "折扣规则");

        validateDTO(dto);

        BeanUtils.copyProperties(dto, rule);
        rule.setConditions(writeConditions(dto.getConditions()));
        rule.setUpdatedAt(LocalDateTime.now());

        discountRuleMapper.updateById(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DiscountRule rule = EntityUtils.requireNonNull(
                discountRuleMapper.selectById(id), "折扣规则");

        rule.setDeleted(1);
        rule.setUpdatedAt(LocalDateTime.now());
        discountRuleMapper.updateById(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        DiscountRule rule = EntityUtils.requireNonNull(
                discountRuleMapper.selectById(id), "折扣规则");

        if (status != 0 && status != 1) {
            throw new BusinessException("状态值无效");
        }

        rule.setStatus(status);
        rule.setUpdatedAt(LocalDateTime.now());
        discountRuleMapper.updateById(rule);
    }

    private void validateDTO(DiscountRuleSaveDTO dto) {
        if (dto.getPriority() == null || dto.getPriority() < 0) {
            throw new BusinessException("优先级不能为负数");
        }
        if (dto.getMaxDiscountAmount() != null && dto.getMaxDiscountAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException("最大优惠金额不能为负数");
        }
        if (dto.getMinPayAmount() != null && dto.getMinPayAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException("最低支付金额不能为负数");
        }
    }

    private DiscountRuleVO toVO(DiscountRule rule) {
        DiscountRuleVO vo = new DiscountRuleVO();
        BeanUtils.copyProperties(rule, vo);
        vo.setConditions(readConditions(rule.getConditions()));
        return vo;
    }

    private Map<String, Object> readConditions(String conditionsJson) {
        if (conditionsJson == null || conditionsJson.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(conditionsJson, MAP_TYPE);
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private String writeConditions(Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(conditions);
        } catch (JsonProcessingException e) {
            throw new BusinessException("条件数据格式错误");
        }
    }

}
