package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberActivityServiceImpl implements MemberActivityService {

    private final MemberActivityMapper memberActivityMapper;

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

        Long total = memberActivityMapper.selectCount(wrapper);

        wrapper.last("LIMIT " + ((current - 1) * size) + "," + size);
        List<MemberActivity> list = memberActivityMapper.selectList(wrapper);

        List<ActivityVO> records = list.stream()
                .map(this::toActivityVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, total, current, size);
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
        MemberActivity activity = new MemberActivity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setType(dto.getType());
        activity.setStartDate(dto.getStartDate());
        activity.setEndDate(dto.getEndDate());
        activity.setCoverImage(dto.getCoverImage());
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
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setType(dto.getType());
        activity.setStartDate(dto.getStartDate());
        activity.setEndDate(dto.getEndDate());
        activity.setCoverImage(dto.getCoverImage());
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
        activity.setDeleted(1);
        memberActivityMapper.updateById(activity);
    }

    private ActivityVO toActivityVO(MemberActivity activity) {
        ActivityVO vo = new ActivityVO();
        vo.setId(activity.getId());
        vo.setTitle(activity.getTitle());
        vo.setDescription(activity.getDescription());
        vo.setType(activity.getType());
        vo.setStartDate(activity.getStartDate());
        vo.setEndDate(activity.getEndDate());
        vo.setCoverImage(activity.getCoverImage());
        vo.setStatus(activity.getStatus());
        vo.setSort(activity.getSort());
        vo.setCreatedAt(activity.getCreatedAt());
        vo.setUpdatedAt(activity.getUpdatedAt());
        return vo;
    }
}
