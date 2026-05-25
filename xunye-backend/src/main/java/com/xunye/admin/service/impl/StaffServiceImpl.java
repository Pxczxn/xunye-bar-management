package com.xunye.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunye.admin.common.BusinessException;
import com.xunye.admin.dto.*;
import com.xunye.admin.entity.StaffUser;
import com.xunye.admin.mapper.StaffUserMapper;
import com.xunye.admin.service.StaffService;
import com.xunye.admin.util.EntityUtils;
import com.xunye.admin.vo.PageResult;
import com.xunye.admin.vo.StaffPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工账号 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffUserMapper staffUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<StaffPageVO> getStaffPage(StaffQueryDTO queryDTO) {
        int currentPage = queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1 : queryDTO.getPageNum();
        int currentSize = queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 ? 10 : Math.min(queryDTO.getPageSize(), 100);
        int offset = (currentPage - 1) * currentSize;

        LambdaQueryWrapper<StaffUser> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w
                    .like(StaffUser::getUsername, queryDTO.getKeyword())
                    .or()
                    .like(StaffUser::getNickname, queryDTO.getKeyword())
            );
        }
        if (StringUtils.hasText(queryDTO.getRole())) {
            wrapper.eq(StaffUser::getRole, queryDTO.getRole());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(StaffUser::getStatus, Integer.parseInt(queryDTO.getStatus()));
        }

        wrapper.orderByDesc(StaffUser::getCreatedAt);

        Page<StaffUser> pageResult = staffUserMapper.selectPage(new Page<>(currentPage, currentSize), wrapper);
        List<StaffUser> list = pageResult.getRecords();
        long total = pageResult.getTotal();

        List<StaffPageVO> voList = list.stream().map(this::toPageVO).collect(Collectors.toList());

        return new PageResult<>(voList, total, currentPage, currentSize);
    }

    @Override
    public StaffPageVO getStaffDetail(Long id) {
        StaffUser user = EntityUtils.requireNonNull(staffUserMapper.selectById(id), "员工");
        return toPageVO(user);
    }

    @Override
    public void createStaff(StaffSaveDTO dto) {
        LambdaQueryWrapper<StaffUser> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(StaffUser::getUsername, dto.getUsername());
        Long exists = staffUserMapper.selectCount(checkWrapper);
        if (exists > 0) {
            throw new BusinessException("用户名已存在");
        }

        StaffUser user = new StaffUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRole(dto.getRole());
        user.setStatus(Integer.parseInt(dto.getStatus()));
        staffUserMapper.insert(user);
    }

    @Override
    public void updateStaff(Long id, StaffUpdateDTO dto) {
        StaffUser user = EntityUtils.requireNonNull(staffUserMapper.selectById(id), "员工");

        user.setNickname(dto.getNickname());
        user.setRole(dto.getRole());
        user.setStatus(Integer.parseInt(dto.getStatus()));
        staffUserMapper.updateById(user);
    }

    @Override
    public void updateStaffStatus(Long id, StaffStatusDTO dto) {
        StaffUser user = EntityUtils.requireNonNull(staffUserMapper.selectById(id), "员工");

        user.setStatus(Integer.parseInt(dto.getStatus()));
        staffUserMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, StaffPasswordDTO dto) {
        StaffUser user = EntityUtils.requireNonNull(staffUserMapper.selectById(id), "员工");

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        staffUserMapper.updateById(user);
    }

    @Override
    public void deleteStaff(Long id) {
        StaffUser user = EntityUtils.requireNonNull(staffUserMapper.selectById(id), "员工");

        staffUserMapper.deleteById(id);
    }

    private StaffPageVO toPageVO(StaffUser user) {
        StaffPageVO vo = new StaffPageVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

}
