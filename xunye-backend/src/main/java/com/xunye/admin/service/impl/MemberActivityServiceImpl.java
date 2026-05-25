package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.ActivitySaveDTO;
import com.xunye.admin.entity.MemberActivity;
import com.xunye.admin.mapper.MemberActivityMapper;
import com.xunye.admin.service.MemberActivityService;
import com.xunye.admin.vo.ActivityVO;
import com.xunye.admin.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberActivityServiceImpl implements MemberActivityService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final MemberActivityMapper memberActivityMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<ActivityVO> getActivityPage(Integer pageNum, Integer pageSize, String keyword, String type, Integer status) {
        int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        LambdaQueryWrapper<MemberActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberActivity::getDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(MemberActivity::getTitle, keyword);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(MemberActivity::getType, type);
        }
        if (status != null) {
            wrapper.eq(MemberActivity::getStatus, status);
        }
        wrapper.orderByAsc(MemberActivity::getSort)
               .orderByDesc(MemberActivity::getCreatedAt);

        Page<MemberActivity> page = new Page<>(current, size);
        Page<MemberActivity> result = memberActivityMapper.selectPage(page, wrapper);

        List<ActivityVO> records = result.getRecords().stream()
                .map(this::toActivityVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, result.getTotal(), current, size);
    }

    @Override
    public ActivityVO getActivityDetail(Long id) {
        MemberActivity activity = memberActivityMapper.selectById(id);
        if (activity == null || activity.getDeleted() == 1) {
            throw new BusinessException(404, "活动不存在");
        }
        return toActivityVO(activity);
    }

    @Override
    public void createActivity(ActivitySaveDTO dto) {
        Map<String, Object> normalizedSettings = ActivitySettingsHelper.normalize(dto.getType(), dto.getSettings());
        MemberActivity activity = new MemberActivity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setType(dto.getType());
        activity.setStartDate(dto.getStartDate());
        activity.setEndDate(dto.getEndDate());
        activity.setCoverImage(dto.getCoverImage());
        activity.setSettings(writeSettings(normalizedSettings));
        activity.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        activity.setSort(dto.getSort() == null ? 0 : dto.getSort());
        memberActivityMapper.insert(activity);
    }

    @Override
    public void updateActivity(Long id, ActivitySaveDTO dto) {
        MemberActivity activity = memberActivityMapper.selectById(id);
        if (activity == null || activity.getDeleted() == 1) {
            throw new BusinessException(404, "活动不存在");
        }
        Map<String, Object> normalizedSettings = ActivitySettingsHelper.normalize(dto.getType(), dto.getSettings());
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setType(dto.getType());
        activity.setStartDate(dto.getStartDate());
        activity.setEndDate(dto.getEndDate());
        activity.setCoverImage(dto.getCoverImage());
        activity.setSettings(writeSettings(normalizedSettings));
        activity.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        activity.setSort(dto.getSort() == null ? 0 : dto.getSort());
        memberActivityMapper.updateById(activity);
    }

    @Override
    public void deleteActivity(Long id) {
        MemberActivity activity = memberActivityMapper.selectById(id);
        if (activity == null || activity.getDeleted() == 1) {
            throw new BusinessException(404, "活动不存在");
        }
        memberActivityMapper.deleteById(id);
    }

    private ActivityVO toActivityVO(MemberActivity activity) {
        Map<String, Object> settings = readSettings(activity.getSettings());
        ActivityVO vo = new ActivityVO();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setDescription(activity.getDescription());
        vo.setType(activity.getType());
        vo.setStartDate(activity.getStartDate());
        vo.setEndDate(activity.getEndDate());
        vo.setCoverImage(activity.getCoverImage());
        vo.setSettings(settings);
        vo.setSettingSummary(ActivitySettingsHelper.summarize(activity.getType(), settings));
        vo.setStatus(activity.getStatus());
        vo.setSort(activity.getSort());
        vo.setCreatedAt(activity.getCreatedAt());
        vo.setUpdatedAt(activity.getUpdatedAt());
        return vo;
    }

    private Map<String, Object> readSettings(String settingsJson) {
        if (!StringUtils.hasText(settingsJson)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(settingsJson, MAP_TYPE);
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "活动配置数据解析失败");
        }
    }

    private String writeSettings(Map<String, Object> settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "活动配置数据保存失败");
        }
    }
}
