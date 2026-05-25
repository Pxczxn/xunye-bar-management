package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.MemberLevelConfigSaveDTO;
import com.xunye.admin.entity.MemberLevelConfig;
import com.xunye.admin.mapper.MemberLevelConfigMapper;
import com.xunye.admin.service.MemberLevelConfigService;
import com.xunye.admin.util.EntityUtils;
import com.xunye.admin.vo.MemberLevelConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberLevelConfigServiceImpl implements MemberLevelConfigService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final MemberLevelConfigMapper memberLevelConfigMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<MemberLevelConfigVO> listAll() {
        LambdaQueryWrapper<MemberLevelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(MemberLevelConfig::getSort)
                .orderByAsc(MemberLevelConfig::getMinAmount);

        return memberLevelConfigMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public MemberLevelConfigVO getDetail(Long id) {
        MemberLevelConfig config = EntityUtils.requireNonNull(
                memberLevelConfigMapper.selectById(id), "会员等级配置");
        return toVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MemberLevelConfigSaveDTO dto) {
        validateDTO(dto);

        LambdaQueryWrapper<MemberLevelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevelConfig::getLevel, dto.getLevel());
        if (memberLevelConfigMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("等级代码已存在");
        }

        MemberLevelConfig config = new MemberLevelConfig();
        BeanUtils.copyProperties(dto, config);
        config.setBenefits(writeBenefits(dto.getBenefits()));
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());

        memberLevelConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MemberLevelConfigSaveDTO dto) {
        MemberLevelConfig config = EntityUtils.requireNonNull(
                memberLevelConfigMapper.selectById(id), "会员等级配置");

        validateDTO(dto);

        if (!config.getLevel().equals(dto.getLevel())) {
            LambdaQueryWrapper<MemberLevelConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MemberLevelConfig::getLevel, dto.getLevel())
                    .ne(MemberLevelConfig::getId, id);
            if (memberLevelConfigMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("等级代码已存在");
            }
        }

        BeanUtils.copyProperties(dto, config);
        config.setBenefits(writeBenefits(dto.getBenefits()));
        config.setUpdatedAt(LocalDateTime.now());

        memberLevelConfigMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MemberLevelConfig config = EntityUtils.requireNonNull(
                memberLevelConfigMapper.selectById(id), "会员等级配置");

        config.setDeleted(1);
        config.setUpdatedAt(LocalDateTime.now());
        memberLevelConfigMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        MemberLevelConfig config = EntityUtils.requireNonNull(
                memberLevelConfigMapper.selectById(id), "会员等级配置");

        if (status != 0 && status != 1) {
            throw new BusinessException("状态值无效");
        }

        config.setStatus(status);
        config.setUpdatedAt(LocalDateTime.now());
        memberLevelConfigMapper.updateById(config);
    }

    private void validateDTO(MemberLevelConfigSaveDTO dto) {
        if (dto.getMinAmount() == null || dto.getMinAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException("最低消费金额不能为负数");
        }
        if (dto.getDiscount() == null || dto.getDiscount().compareTo(java.math.BigDecimal.ZERO) <= 0
                || dto.getDiscount().compareTo(java.math.BigDecimal.ONE) > 0) {
            throw new BusinessException("折扣率必须在0-1之间");
        }
        if (dto.getPointsRate() == null || dto.getPointsRate().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("积分倍率必须大于0");
        }
    }

    private MemberLevelConfigVO toVO(MemberLevelConfig config) {
        MemberLevelConfigVO vo = new MemberLevelConfigVO();
        BeanUtils.copyProperties(config, vo);
        vo.setBenefits(readBenefits(config.getBenefits()));
        return vo;
    }

    private Map<String, Object> readBenefits(String benefitsJson) {
        if (benefitsJson == null || benefitsJson.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(benefitsJson, MAP_TYPE);
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    private String writeBenefits(Map<String, Object> benefits) {
        if (benefits == null || benefits.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(benefits);
        } catch (JsonProcessingException e) {
            throw new BusinessException("会员权益数据格式错误");
        }
    }

}
